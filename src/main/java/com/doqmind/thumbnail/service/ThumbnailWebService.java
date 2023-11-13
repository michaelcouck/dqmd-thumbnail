package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.model.Thumbnail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * ...
 *
 * @author Michael Couck
 * @version 1.0
 * @since 09-11-2023
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(value = "thumbnail-service", tags = {"Endpoint to access thumbnails"})
public class ThumbnailWebService {

    public static final String THUMBNAIL = "/thumbnail";

    private final ThumbnailService thumbnailService;

    @Autowired
    public ThumbnailWebService(final ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    @ResponseBody
    @PreAuthorize("#clientId == authentication.principal")
    @GetMapping(value = THUMBNAIL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the thumbnail by id", notes = "Bla...", response = Collection.class)
    public Thumbnail getThumbnail(@RequestParam(required = false, value = "clientId") final String clientId,
                                  @RequestParam(required = false, value = "thumbnailId") final String thumbnailId,
                                  @RequestParam(required = false, value = "thumbnailPartialName") final String thumbnailPartialName) {
        return thumbnailService.getThumbnail(thumbnailPartialName);
    }


}