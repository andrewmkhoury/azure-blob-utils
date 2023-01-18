# azure-blob-utils

## Copy Blob files between containers

The TransferBlobs feature copies blobs from one container to another in the same storage account.

Here are instructions to build and run this application:

**NOTE:** The application creates a tree structure for the destination files using the file names.  If you would like to edit this functionality, see [here](https://github.com/andrewmkhoury/azure-blob-utils/blob/622a770f07db6a1758e2cd0dd7759da56a77188a/src/main/java/com/adobe/aem/assets/azure/blobutils/AzureBlobCopier.java#L157).  This is to address situations where you need the files to be broken up into a logical folder structure by using parts of the file name.

1. Build the jar file using maven:
   ```
   mvn install
   ```
   This will build a jar file under `target/`
2. Edit the `azure-blob-utils.properties` file, set all the relevant properties:
   ```
   endpoint-url=https://teststorageaccount.blob.core.windows.net
   storage-account=teststorageaccount
   storage-shared-key=
   source-container-name=test-source-blob-container
   destination-container-name=test-destination-blob-container
   max-files-to-copy=20
   ```
   * Instructions to retrieve the `storage-shared-key` can be found [here](https://learn.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage?tabs=azure-portal)
   * If the storage container specified by `destination-container-name` doesn't exist in the `storage-account` then it will be created.
   * The `max-files-to-copy` is set to 20 for testing purposes, so you can first test the copy. Once you are ready to run the process completely then increase the value.
3. Run the jar (the working directory where you run it from must have the `azure-blob-utils.properties` file):
   ```
   java -jar target/azure-blob-utils-0.0.1-SNAPSHOT-jar-with-dependencies.jar
   ```

### Example output

```
Queued for copy: 0000-0bdf-86f6-4694-90a4-3b32fff37e58-1649919551568
Queued for copy: 0000-dbe333e73d25c290554687f0eba2480ee6904f779b37d75b7e1d762a84ae
Queued for copy: 0001-5e38-695c-41aa-919b-eae97d926752-1650350594318
Queued for copy: 0002-0461-4882-42f4-b1f8-4c5269bd9d36-1650350188950
Queued for copy: 0002-fde5-7959-44b8-92a9-3c75ed29e227-1650350419445
Queued for copy: 0003-9adf-159c-4339-9126-4542f5472fda-1652739215056
Queued for copy: 0004-9b78-c78e-481f-baf0-7b86a0b0455e-1652809484250
Queued for copy: 0004-ee71-15ac-415f-b96a-cd3c0ba20917-1650351330497
Queued for copy: 0005-523b-c262-445c-82ab-10198b975e9a-1656031005103
Queued for copy: 0005-d745-2f4f-4037-b87c-b61ca1021117-1652738901127
Copied test-container/0000-0bdf-86f6-4694-90a4-3b32fff37e58-1649919551568 to test-container-copy/0000/0bdf/86f6/00000bdf86f6469490a43b32fff37e581649919551568
Copied test-container/0000-dbe333e73d25c290554687f0eba2480ee6904f779b37d75b7e1d762a84ae to test-container-copy/0000/dbe3/33e7/0000dbe333e73d25c290554687f0eba2480ee6904f779b37d75b7e1d762a84ae
Copied test-container/0001-5e38-695c-41aa-919b-eae97d926752-1650350594318 to test-container-copy/0001/5e38/695c/00015e38695c41aa919beae97d9267521650350594318
Copied test-container/0002-0461-4882-42f4-b1f8-4c5269bd9d36-1650350188950 to test-container-copy/0002/0461/4882/00020461488242f4b1f84c5269bd9d361650350188950
Copied test-container/0002-fde5-7959-44b8-92a9-3c75ed29e227-1650350419445 to test-container-copy/0002/fde5/7959/0002fde5795944b892a93c75ed29e2271650350419445
Copied test-container/0003-9adf-159c-4339-9126-4542f5472fda-1652739215056 to test-container-copy/0003/9adf/159c/00039adf159c433991264542f5472fda1652739215056
Copied test-container/0004-9b78-c78e-481f-baf0-7b86a0b0455e-1652809484250 to test-container-copy/0004/9b78/c78e/00049b78c78e481fbaf07b86a0b0455e1652809484250
Copied test-container/0004-ee71-15ac-415f-b96a-cd3c0ba20917-1650351330497 to test-container-copy/0004/ee71/15ac/0004ee7115ac415fb96acd3c0ba209171650351330497
Copied test-container/0005-523b-c262-445c-82ab-10198b975e9a-1656031005103 to test-container-copy/0005/523b/c262/0005523bc262445c82ab10198b975e9a1656031005103
Copied test-container/0005-d745-2f4f-4037-b87c-b61ca1021117-1652738901127 to test-container-copy/0005/d745/2f4f/0005d7452f4f4037b87cb61ca10211171652738901127
```
