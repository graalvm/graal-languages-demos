/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.net.URI;
import java.net.URISyntaxException;

@Controller
public class DemoController {
    private final PhotonService photonService;

    public DemoController(PhotonService photonService) {
        this.photonService = photonService;
    }

    @Get("/photo/{effectName}")
    @Produces(MediaType.IMAGE_PNG)
    @ExecuteOn(TaskExecutors.BLOCKING)
    public byte[] renderPhoto(@Parameter String effectName) {
        return photonService.processImage(effectName);
    }

    @Get("/")
    public HttpResponse<?> index() throws URISyntaxException {
        return HttpResponse.redirect(new URI("/index.html"));
    }
}
