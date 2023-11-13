package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.model.Thumbnail;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
public interface ThumbnailService {

    void crawlBlob(int depth);

    Thumbnail getThumbnail(final String name);

    boolean deleteThumbnail(final String name);

}