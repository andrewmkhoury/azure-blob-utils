package com.adobe.aem.assets.azure.blobutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TransferContext {
		private static final String PROP_FILE_PATH = "azure-blob-utils.properties";

		private String endpointUrl;
		private String storageAccount;
		private String storageSharedKey;
		private String sourceContainerName;
		private String destinationContainerName;
		private int maxFilesToCopy = -1;

		public TransferContext() {
			readPropertiesFileAndInitialize();
		}

		private void readPropertiesFileAndInitialize() {
			try (InputStream input = new FileInputStream(PROP_FILE_PATH)) {

	            Properties prop = new Properties();
	            prop.load(input);
				setEndpointUrl(prop.getProperty("endpoint-url"));
				setStorageAccount(prop.getProperty("storage-account"));
				setStorageSharedKey(prop.getProperty("storage-shared-key"));
				setSourceContainerName(prop.getProperty("source-container-name"));
				setDestinationContainerName(prop.getProperty("destination-container-name"));
				setMaxFilesToCopy(Integer.parseInt(prop.getProperty("max-files-to-copy")));
			} catch (FileNotFoundException e) {
				System.out.println(String.format("Properties file %s not found", PROP_FILE_PATH));
			} catch (IOException e) {
				System.out.println(String.format("Error reading properties file %s", PROP_FILE_PATH));
			}
		}
		
		public boolean isContextValid() {
			if (isNullOrEmpty(this.getEndpointUrl()) ||
					isNullOrEmpty(this.getStorageAccount()) ||
					isNullOrEmpty(this.getStorageSharedKey()) ||
					isNullOrEmpty(this.getSourceContainerName()) ||
					isNullOrEmpty(this.getDestinationContainerName()) ||
					this.getMaxFilesToCopy() == -1) {
				return false;
			} else {
				return true;
			}
		}
		
		private boolean isNullOrEmpty(String propValue) {
			if(propValue == null || propValue.trim().length() == 0) {
				return true;
			}
			return false;
		}

		public String getEndpointUrl() {
			return endpointUrl;
		}
		public void setEndpointUrl(String endpointUrl) {
			this.endpointUrl = endpointUrl;
		}
		public String getStorageAccount() {
			return storageAccount;
		}
		public void setStorageAccount(String storageAccount) {
			this.storageAccount = storageAccount;
		}
		public String getStorageSharedKey() {
			return storageSharedKey;
		}
		public void setStorageSharedKey(String storageSharedKey) {
			this.storageSharedKey = storageSharedKey;
		}
		public String getDestinationContainerName() {
			return destinationContainerName;
		}
		public void setDestinationContainerName(String destinationContainerName) {
			this.destinationContainerName = destinationContainerName;
		}
		public String getSourceContainerName() {
			return sourceContainerName;
		}
		public void setSourceContainerName(String sourceContainerName) {
			this.sourceContainerName = sourceContainerName;
		}
		public int getMaxFilesToCopy() {
			return maxFilesToCopy;
		}
		public void setMaxFilesToCopy(int maxFilesToCopy) {
			this.maxFilesToCopy = maxFilesToCopy;
		}

}
