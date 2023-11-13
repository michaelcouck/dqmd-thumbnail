package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.conf.ConfigurationProperties;
import com.doqmind.thumbnail.model.Asset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 13-11-2023
 */
@Slf4j
@Service
public class FileUploadListener {

    @Value("${" + ConfigurationProperties.THUMBNAIL_TOPIC + "}")
    protected String thumbnailTopicName;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ThumbnailService thumbnailService;

    @Autowired
    public FileUploadListener(final ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    @Transactional
    @JmsListener(
            destination = "${" + ConfigurationProperties.THUMBNAIL_TOPIC + "}",
            containerFactory = "jmsListenerContainerFactory",
            selector = "typeId='Asset'",
            subscription = "thumbnail-topic")
    public void generateThumbnailForAsset(@Payload final Asset asset) {
        // Generate the thumbnail for the asset
        thumbnailService.getThumbnail(asset.getName(), Boolean.TRUE);
    }

}