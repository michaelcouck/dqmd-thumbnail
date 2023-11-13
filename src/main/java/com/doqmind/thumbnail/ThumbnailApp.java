package com.doqmind.thumbnail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
@EnableAsync
@SpringBootConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.doqmind"})
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@Profile({"default", "dev", "test", "uat", "prd"})
public class ThumbnailApp {

    public static void main(final String[] args) {
        SpringApplication.run(ThumbnailApp.class);
    }

}