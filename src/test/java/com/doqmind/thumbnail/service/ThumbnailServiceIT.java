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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

    @LocalServerPort
    @SuppressWarnings("unused")
    private int port;

    @Autowired
    private ThumbnailService thumbnailService;

    @BeforeEach
    public void before() {
    }

    @Test
    public void crawlBlob() {
        String thumbnailName = "/mnt/sda/Workspace/doqmind/thumbnail/src/test/resources/assets/testFile1.pdf-512x.jpg";
        thumbnailService.deleteThumbnail(thumbnailName);
        Thumbnail thumbnail = thumbnailService.getThumbnail(thumbnailName);
        Assertions.assertNull(thumbnail.getBlob());

        thumbnailService.crawlBlob(7);
        thumbnail = thumbnailService.getThumbnail(thumbnailName);
        Assertions.assertNotNull(thumbnail.getBlob());
    }

}