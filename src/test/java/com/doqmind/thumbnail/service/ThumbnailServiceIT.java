package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.ThumbnailApp;
import com.doqmind.thumbnail.model.Thumbnail;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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

    @Test
    public void getThumbnail() throws IOException, InterruptedException {
        String[] testFiles = {"testFile1.pdf", "testFile4.pdf", "testFile99.pdf", "testFile101.pdf"};
        for (final String assetName : testFiles) {
            thumbnailService.deleteThumbnail(assetName);
            Thumbnail thumbnail = thumbnailService.getThumbnail(assetName, false);
            Assertions.assertNull(thumbnail.getBlob());

            thumbnail = thumbnailService.getThumbnail(assetName, true);
            Assertions.assertNotNull(thumbnail.getBlob());
        }
    }

}