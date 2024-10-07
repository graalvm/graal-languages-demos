/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller
public class DemoController {

    private final PyGalServicePurePython pyGalServicePurePython;
    private final PyGalServicePureJava pyGalServicePureJava;
    private final PyGalServiceMixed pyGalServiceMixed;
    private final PyGalServiceValueAPIDynamic pyGalServiceValueAPIDynamic;

    DemoController(PyGalServicePurePython pyGalServicePurePython, PyGalServicePureJava pyGalServicePureJava, PyGalServiceMixed pyGalServiceMixed, PyGalServiceValueAPIDynamic pyGalServiceValueAPIDynamic) {
        this.pyGalServicePurePython = pyGalServicePurePython;
        this.pyGalServicePureJava = pyGalServicePureJava;
        this.pyGalServiceMixed = pyGalServiceMixed;
        this.pyGalServiceValueAPIDynamic = pyGalServiceValueAPIDynamic;
    }

    @Get("/python")
    @Produces(MediaType.IMAGE_SVG)
    public String renderXYChartPurePython() {
        return pyGalServicePurePython.renderXYChart();
    }

    @Get("/java")
    @Produces(MediaType.IMAGE_SVG)
    public String renderXYChartPureJava() {
        return pyGalServicePureJava.renderXYChart();
    }

    @Get("/mixed")
    @Produces(MediaType.IMAGE_SVG)
    public String renderXYChartMixed() {
        return pyGalServiceMixed.renderXYChart();
    }

    @Get("/dynamic")
    @Produces(MediaType.IMAGE_SVG)
    public String renderXYChartValueAPIDynamic() {
        return pyGalServiceValueAPIDynamic.renderXYChart();
    }
}

