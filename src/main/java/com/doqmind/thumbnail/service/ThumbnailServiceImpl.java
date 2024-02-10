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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Michael Couck
 * @version 1.0
 * @see ThumbnailService
 * @since 09-11-2023
 */
@SuppressWarnings("ConstantConditions")
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

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient blobContainerClient;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ThumbnailRepository thumbnailRepository;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    public ThumbnailServiceImpl(final ThumbnailRepository thumbnailRepository) {
        this.thumbnailRepository = thumbnailRepository;
    }

    @Override
    public Thumbnail getThumbnail(final String originalAssetName, final boolean createIfNotExists) throws IOException, InterruptedException {
        String thumbnailName = originalAssetName + THUMBNAIL_SUFFIX;
        BlobContainerClient blobContainerClient = getBlobContainerClient();
        BlobClient thumbnailBlobClient = blobContainerClient.getBlobClient(thumbnailName);
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
            return Thumbnail.builder().name(thumbnailName).blob(IOUtils.toByteArray(inputStream)).build();
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
    @SuppressWarnings({"Convert2Lambda"})
    public void generateThumbnails() {
        log.info("Crawling blob: " + container);
        final BlobContainerClient blobContainerClient = getBlobContainerClient();
        PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs();
        blobItems.forEach(new Consumer<BlobItem>() {
            @SneakyThrows
            @Override
            public void accept(final BlobItem blobItem) {
                BlobClient blobClient = blobContainerClient.getBlobClient(blobItem.getName());
                generateThumbnail(blobContainerClient, blobClient);
            }
        });
    }

    private void generateThumbnail(final BlobContainerClient blobContainerClient, final BlobClient blobClient) throws IOException, InterruptedException {
        log.info("Blob file: " + blobClient.getBlobName());
        if (!blobClient.getBlobName().endsWith(THUMBNAIL_SUFFIX)) {
            File downloadedFile = new File(blobClient.getBlobName());
            String thumbnailName = blobClient.getBlobName() + THUMBNAIL_SUFFIX;

            BlobClient thumbnailBlobClient = blobContainerClient.getBlobClient(thumbnailName);
            if (!thumbnailBlobClient.exists()) {
                downloadBlobFile(downloadedFile, blobClient);
                generateThumbnail(thumbnailName, downloadedFile);
                uploadThumbnail(thumbnailName, thumbnailBlobClient);
            } else {
                log.info("Thumbnail already generated: " + thumbnailName);
            }
            // TODO: The delete must be uncommented for production, this is just for testing
            boolean deleted = false; // FileUtils.deleteQuietly(downloadedFile);
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

    private void generateThumbnail(final String thumbnailName, final File downloadedFile) throws InterruptedException, IOException {
        log.info("Generating thumbnail: " + thumbnailName);
        final Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec("convert -thumbnail x512 -background white -alpha remove " + downloadedFile.getAbsolutePath() + "[0] " + thumbnailName);
        boolean exitValue = process.waitFor(5, TimeUnit.MINUTES);
        log.info("Process exit value for thumbnail generation: " + exitValue);

        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private void uploadThumbnail(final String thumbnailName, final BlobClient thumbnailBlobClient) throws IOException {
        File thumbnailFile = new File(thumbnailName);
        if (thumbnailFile.exists()) {
            log.info("Uploading thumbnail: " + thumbnailName);
            try (FileInputStream fileInputStrem = new FileInputStream(thumbnailFile)) {
                thumbnailBlobClient.upload(fileInputStrem);
            }
            // TODO: The delete must be uncommented for production, this is just for testing
            boolean deletedThumbnailFile = false; // FileUtils.deleteQuietly(new File(thumbnailName));
            if (!deletedThumbnailFile) {
                log.warn("Didn't delete thumbnail locally: " + thumbnailName);
            }
        } else {
            log.warn("Thumbnail file does not exist: {}", thumbnailFile);
        }
    }

    protected BlobContainerClient getBlobContainerClient() {
        if (blobServiceClient == null) {
            StorageSharedKeyCredential storageSharedKeyCredential = StorageSharedKeyCredential.fromConnectionString(connectionString);
            blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(containerUrl)
                    .credential(storageSharedKeyCredential)
                    .buildClient();
        }
        if (blobContainerClient == null) {
            blobContainerClient = blobServiceClient.getBlobContainerClient(container);
        }
        return blobContainerClient;
    }

}