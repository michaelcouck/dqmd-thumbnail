package com.doqmind.thumbnail.service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.doqmind.thumbnail.database.ThumbnailRepository;
import com.doqmind.thumbnail.model.Thumbnail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * @author Michael Couck
 * @version 1.0
 * @see ThumbnailService
 * @since 09-11-2023
 */
@Slf4j
@Service
public class ThumbnailServiceImpl implements ThumbnailService {

    @Value("${thumbnail.container-url}")
    private String containerUrl;
    @Value("${thumbnail.connection-string}")
    private String connectionString;
    @Value("${thumbnail.container}")
    private String container;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ThumbnailRepository thumbnailRepository;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public ThumbnailServiceImpl(final ThumbnailRepository thumbnailRepository) {
        this.thumbnailRepository = thumbnailRepository;
    }

    @Override
    public Thumbnail getThumbnail(final String name) {
        BlobContainerClient blobContainerClient = getBlobContainerClient();
        BlobClient blobClient = blobContainerClient.getBlobClient(name);
        if (blobClient.exists()) {
            InputStream inputStream = blobClient.openInputStream();
            try {
                return Thumbnail
                        .builder()
                        .blob(inputStream.readAllBytes())
                        .build();
            } catch (final IOException e) {
                log.error("Error reading from thumbnail in blob storage: " + name, e);
            }
        }
        return Thumbnail.builder().build();
    }

    @Override
    public boolean deleteThumbnail(final String name) {
        BlobContainerClient blobContainerClient = getBlobContainerClient();
        BlobClient blobClient = blobContainerClient.getBlobClient(name);
        return blobClient.deleteIfExists();
    }

    @Override
    @SuppressWarnings({"Convert2Lambda", "Convert2Diamond"})
    public void crawlBlob(int depth) {
        log.info("Crawling blob: " + depth);
        final BlobContainerClient blobContainerClient = getBlobContainerClient();
        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs();
        blobItems.forEach(new Consumer<BlobItem>() {
            @Override
            public void accept(final BlobItem blobItem) {
                BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
                log.info("Blob file: " + blobClient.getBlobName());
                if (!blobClient.getBlobName().endsWith("jpg")) {
                    File downloadedFile = new File(blobClient.getBlobName());
                    String downloadedFilePath = downloadedFile.getAbsolutePath();
                    String thumbnailFilePath = downloadedFilePath + "-512x.jpg";

                    BlobClient thumbnailBlobClient = blobContainerClient.getBlobClient(thumbnailFilePath);
                    try {
                        if (!thumbnailBlobClient.exists()) {
                            downloadBlobFile(downloadedFile, blobClient);
                            generateThumbnail(downloadedFilePath, thumbnailFilePath, thumbnailBlobClient);
                        } else {
                            log.info("Thumbnail already generated: " + thumbnailFilePath);
                        }
                    } catch (final IOException | InterruptedException e) {
                        log.error("Error generating & uploading thumbnail: " + thumbnailFilePath, e);
                    }
                    boolean deleted = FileUtils.deleteQuietly(downloadedFile);
                    if (!deleted) {
                        log.info("File not deleted: " + downloadedFile);
                    }
                }
            }
        });
    }

    private void downloadBlobFile(final File downloadedFile, final BlobClient blobClient) {
        if (!downloadedFile.exists()) {
            try {
                boolean createdLocalFile = downloadedFile.createNewFile();
                if (createdLocalFile) {
                    InputStream inputStream = blobClient.openInputStream();
                    FileUtils.copyInputStreamToFile(inputStream, downloadedFile);
                } else {
                    log.warn("Didn't create local file: " + downloadedFile);
                }
            } catch (final IOException e) {
                log.error("Exception processing file from blob", e);
            }
        }
    }

    private void generateThumbnail(final String downloadedFilePath, final String thumbnailFilePath, final BlobClient thumbnailBlobClient) throws InterruptedException, IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("convert -thumbnail x512 -background white -alpha remove " + downloadedFilePath + "[0] " + thumbnailFilePath);
        int exitValue = process.waitFor();
        log.info("Process exit value for thumbnail generation: " + exitValue);
        thumbnailBlobClient.uploadFromFile(thumbnailFilePath, Boolean.FALSE);
        log.info("Uploaded thumbnail: " + thumbnailFilePath);
        boolean deletedThumbnailFile = FileUtils.deleteQuietly(new File(thumbnailFilePath));
        if (!deletedThumbnailFile) {
            log.warn("Didn't delete thumbnail locally: " + thumbnailFilePath);
        }
    }

    private BlobContainerClient getBlobContainerClient() {
        StorageSharedKeyCredential storageSharedKeyCredential = StorageSharedKeyCredential.fromConnectionString(connectionString);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(containerUrl)
                .credential(storageSharedKeyCredential)
                .buildClient();
        return blobServiceClient.getBlobContainerClient(container);
    }

}