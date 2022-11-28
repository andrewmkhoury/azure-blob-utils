package com.adobe.aem.assets.azure.blobutils;

public class TransferBlobs {

	public static void main(String[] args) {
		TransferContext ctx = new TransferContext();
		// if any properties aren't defined in azure-blob-utils.properties then we fail
		if (!ctx.isContextValid()) {
			printHelp();
			System.exit(1);
		}

		AzureBlobCopier azBlobCopier = new AzureBlobCopier(ctx);
		azBlobCopier.runBlobTransfer();
	}

	private static void printHelp() {
		System.out.println("Update the azure-blob-utils.properties file with the correct blob container info");
	}

}
