package com.doqmind.thumbnail.service;

import com.doqmind.thumbnail.model.Thumbnail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    @SuppressWarnings({"DefaultAnnotationParam", "unused"})
    // @PreAuthorize("#clientId == authentication.principal")
    @GetMapping(value = THUMBNAIL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the thumbnail by original asset name", notes = "Bla...", response = Thumbnail.class)
    public Thumbnail getThumbnail(@RequestParam(required = true, value = "clientId") final String clientId,
                                  @RequestParam(required = true, value = "originalAssetName") final String originalAssetName) throws IOException, InterruptedException {
        return thumbnailService.getThumbnail(originalAssetName, true);
    }


}