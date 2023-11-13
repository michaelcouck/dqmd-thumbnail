package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.model.Thumbnail;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
public interface ThumbnailService {

    void crawlBlob();

    Thumbnail getThumbnail(final String originalAssetName, final boolean createIfNotExists);

    boolean deleteThumbnail(final String originalAssetName);

}