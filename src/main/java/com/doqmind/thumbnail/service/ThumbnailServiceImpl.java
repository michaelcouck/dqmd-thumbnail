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
import java.io.FileInputStream;
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

    private static final String THUMBNAIL_SUFFIX = "-512x.jpg";

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
    public Thumbnail getThumbnail(final String originalAssetName, final boolean createIfNotExists) {
        BlobContainerClient blobContainerClient = getBlobContainerClient();
        BlobClient thumbnailBlobClient = blobContainerClient.getBlobClient(originalAssetName + THUMBNAIL_SUFFIX);
        if (!thumbnailBlobClient.exists() & createIfNotExists) {
            BlobClient assetBlobClient = blobContainerClient.getBlobClient(originalAssetName);
            if (assetBlobClient.exists()) {
                generateThumbnail(blobContainerClient, assetBlobClient);
            } else {
                log.warn("No such asset: " + originalAssetName);
            }
        }
        if (thumbnailBlobClient.exists()) {
            InputStream inputStream = thumbnailBlobClient.openInputStream();
            try {
                return Thumbnail
                        .builder()
                        .blob(inputStream.readAllBytes())
                        .build();
            } catch (final IOException e) {
                log.error("Error reading from thumbnail in blob storage: " + originalAssetName, e);
            }
        }
        return Thumbnail.builder().build();
    }

    @Override
    public boolean deleteThumbnail(final String originalAssetName) {
        BlobContainerClient blobContainerClient = getBlobContainerClient();
        BlobClient blobClient = blobContainerClient.getBlobClient(originalAssetName + THUMBNAIL_SUFFIX);
        return blobClient.deleteIfExists();
    }

    @Override
    @SuppressWarnings({"Convert2Lambda", "Convert2Diamond"})
    public void crawlBlob() {
        log.info("Crawling blob: " + container);
        final BlobContainerClient blobContainerClient = getBlobContainerClient();
        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs();
        blobItems.forEach(new Consumer<BlobItem>() {
            @Override
            public void accept(final BlobItem blobItem) {
                BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
                generateThumbnail(blobContainerClient, blobClient);
            }
        });
    }

    private void generateThumbnail(final BlobContainerClient blobContainerClient, final BlobClient blobClient) {
        log.info("Blob file: " + blobClient.getBlobName());
        if (!blobClient.getBlobName().endsWith("jpg")) {
            File downloadedFile = new File(blobClient.getBlobName());
            String thumbnailName = blobClient.getBlobName() + THUMBNAIL_SUFFIX;

            BlobClient thumbnailBlobClient = blobContainerClient.getBlobClient(thumbnailName);
            try {
                if (!thumbnailBlobClient.exists()) {
                    downloadBlobFile(downloadedFile, blobClient);
                    generateThumbnail(thumbnailName, downloadedFile, thumbnailBlobClient);
                } else {
                    log.info("Thumbnail already generated: " + thumbnailName);
                }
            } catch (final IOException | InterruptedException e) {
                log.error("Error generating & uploading thumbnail: " + thumbnailName, e);
            }
            boolean deleted = FileUtils.deleteQuietly(downloadedFile);
            if (!deleted) {
                log.info("File not deleted: " + downloadedFile);
            }
        }
    }

    private void downloadBlobFile(final File downloadedFile, final BlobClient blobClient) {
        if (!downloadedFile.exists()) {
            try {
                log.info("Downloading file: " + downloadedFile);
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

    private void generateThumbnail(final String thumbnailName, final File downloadedFile, final BlobClient thumbnailBlobClient) throws InterruptedException, IOException {
        log.info("Generating thumbnail: " + thumbnailName);
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("convert -thumbnail x512 -background white -alpha remove " + downloadedFile.getAbsolutePath() + "[0] " + thumbnailName);
        int exitValue = process.waitFor();
        log.info("Process exit value for thumbnail generation: " + exitValue);

        log.info("Uploading thumbnail: " + downloadedFile);
        thumbnailBlobClient.upload(new FileInputStream(thumbnailName));
        boolean deletedThumbnailFile = FileUtils.deleteQuietly(new File(thumbnailName));
        if (!deletedThumbnailFile) {
            log.warn("Didn't delete thumbnail locally: " + thumbnailName);
        }
    }

    protected BlobContainerClient getBlobContainerClient() {
        StorageSharedKeyCredential storageSharedKeyCredential = StorageSharedKeyCredential.fromConnectionString(connectionString);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(containerUrl)
                .credential(storageSharedKeyCredential)
                .buildClient();
        return blobServiceClient.getBlobContainerClient(container);
    }

}