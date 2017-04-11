package com.yoshio3.services;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by yoterada on 2017/04/05.
 */

@Service
public class StorageService{

    private static final Logger LOGGER = Logger.getLogger(StorageService.class.getName());

    @Value("${azstorage.CONTAINER_NAME}")
    private String containerName;

    @Value("${azstorage.STORAGE_CONNECTION_STRING}")
    private String connectionString;

    private CloudBlobClient blobClient;

    //初期化
    @PostConstruct
    public void init() {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
            blobClient = storageAccount.createCloudBlobClient();
            createContainer(containerName);
        } catch (URISyntaxException | InvalidKeyException ex) {
            LOGGER.log(Level.SEVERE, "Invalid Account", ex);
        }
    }

    //ディレクトリ(コンテナ)の新規作成
    private void createContainer(String containerName) {
        try {
            String lowercase = containerName.toLowerCase(); //if it include Uppercase 400 error
            CloudBlobContainer container = blobClient.getContainerReference(lowercase);
            if (!container.exists()) {
                container.createIfNotExists();

                BlobContainerPermissions permissions = new BlobContainerPermissions();
                permissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
                container.uploadPermissions(permissions);
            }
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid URISyntax", ex);
        } catch (StorageException ste) {
            LOGGER.log(Level.SEVERE, "Invalid Strage type", ste);
        }
    }

    //ファイルのアップロード
    public void uploadFile(byte[] file, String fileName) throws URISyntaxException, StorageException, IOException{
        CloudBlobContainer container;
        container = blobClient.getContainerReference(containerName);
        CloudBlockBlob blob = container.getBlockBlobReference(fileName);
        blob.upload(new ByteArrayInputStream(file), file.length);
    }

    //ファイルの一覧取得
    public List<BlobStorageEntity> getAllFiles() throws URISyntaxException, StorageException{
        List<BlobStorageEntity> entity = new ArrayList<>();

        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        Iterable<ListBlobItem> items = container.listBlobs();
        Spliterator<ListBlobItem> spliterator = items.spliterator();
        Stream<ListBlobItem> stream = StreamSupport.stream(spliterator, false);

        List<CloudBlob> blockBlob = stream
                .filter(item -> item instanceof CloudBlob)
                .map(item -> (CloudBlob) item)
                .collect(Collectors.toList());

        entity = blockBlob.stream().map(blob -> convertEntity(blob)).collect(Collectors.toList());

        return entity;
    }

    //表示項目出力用のビーンに変換
    private BlobStorageEntity convertEntity(CloudBlob blob) {
        BlobStorageEntity entity = new BlobStorageEntity();
        try {

            BlobProperties properties = blob.getProperties();

            entity.setLastModifyDate(properties.getLastModified());
            entity.setName(blob.getName());
            entity.setSize(properties.getLength());
            entity.setURI(blob.getUri().toString());

        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return entity;
    }

    public void deleteAll(String containerName) {
        try {
            CloudBlobContainer container = blobClient.getContainerReference(containerName);
            Iterable<ListBlobItem> items = container.listBlobs();
            Spliterator<ListBlobItem> spliterator = items.spliterator();
            Stream<ListBlobItem> stream = StreamSupport.stream(spliterator, false);

            stream.filter(item -> item instanceof CloudBlob)
                    .map(item -> (CloudBlob) item)
                    .forEach(blob -> {
                        try {
                            String name = blob.getName();

                            CloudBlockBlob delFile;
                            delFile = container.getBlockBlobReference(name);
                            // Delete the blob.
                            delFile.deleteIfExists();
                        } catch (URISyntaxException | StorageException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    });
        } catch (URISyntaxException | StorageException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}