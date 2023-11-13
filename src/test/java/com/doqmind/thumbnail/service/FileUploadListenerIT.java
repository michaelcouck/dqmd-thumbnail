package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.ThumbnailApp;
import com.doqmind.thumbnail.conf.ActiveMQConfiguration;
import com.doqmind.thumbnail.model.Asset;
import com.doqmind.thumbnail.model.Thumbnail;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.JMSException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 10-11-2023
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ThumbnailApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
public class FileUploadListenerIT {

    @LocalServerPort
    @SuppressWarnings("unused")
    private int port;

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQConfiguration activeMQConfiguration;
    @Autowired
    private ThumbnailService thumbnailService;

    @BeforeEach
    public void before() {
    }

    @Test
    public void generateThumbnailForAsset() throws JMSException, InterruptedException {
        String originalAssetName = "testFile1.pdf";
        thumbnailService.deleteThumbnail(originalAssetName);
        Thumbnail thumbnail = thumbnailService.getThumbnail(originalAssetName, false);
        Assertions.assertNull(thumbnail.getBlob());

        // Put a message on the thumbnail topic
        Asset asset = Asset.builder().name(originalAssetName).build();
        jmsTemplate.convertAndSend(activeMQConfiguration.getThumbnailTopic(), asset, message -> {
            message.setJMSCorrelationID(UUID.randomUUID().toString());
            message.setStringProperty("typeId", Asset.class.getSimpleName());
            return message;
        });

        int secondsToWait = 30;
        while (secondsToWait-- > 0) {
            thumbnail = thumbnailService.getThumbnail(originalAssetName, false);
            if (thumbnail.getBlob() != null) {
                break;
            }
            Thread.sleep(1000);
        }
        Assertions.assertNotNull(thumbnail.getBlob());
    }

}