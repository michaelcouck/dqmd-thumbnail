package com.doqmind.thumbnail.schedule;

import com.doqmind.thumbnail.database.ThumbnailRepository;
import com.doqmind.thumbnail.service.ThumbnailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.transaction.Transactional;

/**
 * Any scheduled tasks in here for thumbnails.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
@Slf4j
@Component
@EnableScheduling
@EnableTransactionManagement
@SuppressWarnings("FieldMayBeFinal")
public class ThumbnailScheduler {

    private final ThumbnailService thumbnailService;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ThumbnailRepository thumbnailRepository;

    @Autowired
    public ThumbnailScheduler(final ThumbnailService thumbnailService, final ThumbnailRepository thumbnailRepository) {
        this.thumbnailService = thumbnailService;
        this.thumbnailRepository = thumbnailRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?") // Fires at 3am everyday
    public void generateThumbnails() {
        log.info("Starting thumbnail scheduled task : generateThumbnails");
        thumbnailService.crawlBlob(7);
    }

}