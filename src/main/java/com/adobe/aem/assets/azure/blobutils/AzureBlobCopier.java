package com.adobe.aem.assets.azure.blobutils;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;

public class AzureBlobCopier {

	private final BlockingQueue<Runnable> linkedBlockingDeque;
	private final ExecutorService executorService;

	TransferContext ctx;

	public AzureBlobCopier(TransferContext ctx) {
		linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(5000000);
		executorService = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, linkedBlockingDeque,
				new ThreadPoolExecutor.CallerRunsPolicy());
		addThreadPoolShutdownHook();

		this.ctx = ctx;
	}

	private void addThreadPoolShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				shutdownHook(executorService);
			}
		});
	}

	public void runBlobTransfer() {
		int maxBlobsToCopy = ctx.getMaxFilesToCopy();

		StorageSharedKeyCredential sskc = new StorageSharedKeyCredential(ctx.getStorageAccount(),
				ctx.getStorageSharedKey());
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(ctx.getEndpointUrl())
				.credential(sskc).buildClient();
		String srcContainerName = ctx.getSourceContainerName();
		String dstContainerName = ctx.getDestinationContainerName();

		BlobContainerClient srcContainer = getBlobContainer(blobServiceClient, srcContainerName);
		BlobContainerClient dstContainer = getOrCreateContainer(blobServiceClient, dstContainerName);

		if (srcContainer != null) {
			if(dstContainer != null) {
				transferBlobs(maxBlobsToCopy, srcContainer, dstContainer);				
			} else {
				System.out.println(String.format("Unable to retrieve or create destination container %s", dstContainerName));				
			}
		} else {
			System.out.println(String.format("Source container %s doesn't exist", srcContainerName));
		}
		awaitTerminationAfterShutdown(executorService);
	}

	private BlobServiceSasSignatureValues getBlobSasSignatureValues(String name) {
		BlobSasPermission permissions = BlobSasPermission.parse("r");
		BlobServiceSasSignatureValues bsssv = new BlobServiceSasSignatureValues(OffsetDateTime.now().plusHours(2),
				permissions);
		return bsssv;
	}

	private void transferBlobs(int maxBlobsToCopy, BlobContainerClient srcContainer, BlobContainerClient dstContainer) {
		PagedIterable<BlobItem> pgIter = srcContainer.listBlobs();
		int counter = 0;
		for (BlobItem blob : pgIter) {
			BlockBlobClient srcBlobClient = srcContainer.getBlobClient(blob.getName()).getBlockBlobClient();
			String newName = generateDestinationName(blob.getName());
			BlockBlobClient dstBlobClient = dstContainer.getBlobClient(newName).getBlockBlobClient();

			String srcBlobSas = srcBlobClient.generateSas(getBlobSasSignatureValues(blob.getName()));
			String srcBlobURL = srcBlobClient.getBlobUrl() + "?" + srcBlobSas;
			System.out.println(String.format("Queued for copy: %s", blob.getName()));
			queuePolling(dstBlobClient.beginCopy(new BlobBeginCopyOptions(srcBlobURL)), blob.getName(), dstContainer.getBlobContainerName(), newName);
			counter++;
			if (counter == maxBlobsToCopy) {
				break;
			}
		}
	}

	private BlobContainerClient getOrCreateContainer(BlobServiceClient blobServiceClient, String containerName) {
		BlobContainerClient containerClient = null;

		/* Create a new container client */
		try {
			containerClient = blobServiceClient.createBlobContainer(containerName);
		} catch (BlobStorageException ex) {
			// The container may already exist, so don't throw an error
			if (!ex.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
				throw ex;
			} else {
				containerClient = getBlobContainer(blobServiceClient, containerName);
			}
		}
		return containerClient;
	}

	private BlobContainerClient getBlobContainer(BlobServiceClient blobServiceClient, String containerName) {
		return blobServiceClient.getBlobContainerClient(containerName);
	}

	private void queuePolling(SyncPoller<BlobCopyInfo, Void> poller, String srcName, String destContainerName, String destName) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				PollResponse<BlobCopyInfo> response = poller.waitForCompletion();
				if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
					System.out.println(String.format("Copied %s to %s/%s", response.getValue().getCopySourceUrl().replaceAll("https:\\/\\/[^/]+\\/|\\?.*", ""), destContainerName, destName));
				} else {
					System.out.println(String.format("ERROR: failed to copy %s", response.getValue()));
				}
			}
		});
	}

	private void shutdownHook(ExecutorService threadPool) {
		threadPool.shutdownNow();
	}

	private void awaitTerminationAfterShutdown(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * This method generates the new destination name for the file.
	 * Customize this method to meet the needs of your application.
	 */
	private String generateDestinationName(String name) {
		String newName = name.replaceAll("-", "");
		if (name.length() > 12) {
			return newName.substring(0, 4) + "/" + newName.substring(4, 8) + "/" + newName.substring(8, 12) + "/"
					+ newName;
		}
		return name;
	}
}
