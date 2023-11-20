package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.ThumbnailApp;
import com.doqmind.thumbnail.model.Thumbnail;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 10-11-2023
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ThumbnailApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
public class ThumbnailServiceIT {

    @Autowired
    private ThumbnailService thumbnailService;

    @BeforeEach
    public void before() {
    }

    @Test
    public void crawlBlob() throws IOException, InterruptedException {
        String originalAssetName = "testFile1.pdf";
        thumbnailService.deleteThumbnail(originalAssetName);
        Thumbnail thumbnail = thumbnailService.getThumbnail(originalAssetName, false);
        Assertions.assertNull(thumbnail.getBlob());

        thumbnailService.crawlBlob();

        thumbnail = thumbnailService.getThumbnail(originalAssetName, false);
        Assertions.assertNotNull(thumbnail.getBlob());
    }

}