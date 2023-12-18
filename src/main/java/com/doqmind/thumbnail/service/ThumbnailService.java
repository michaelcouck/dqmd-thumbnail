package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.model.Thumbnail;

import java.io.IOException;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
public interface ThumbnailService {

    void generateThumbnails();

    Thumbnail getThumbnail(final String originalAssetName, final boolean createIfNotExists) throws IOException, InterruptedException;

    boolean deleteThumbnail(final String originalAssetName);

}