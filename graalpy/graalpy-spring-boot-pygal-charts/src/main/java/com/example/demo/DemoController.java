/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final PyGalServicePurePython pyGalServicePurePython;
    private final PyGalServicePureJava pyGalServicePureJava;
    private final PyGalServiceMixed pyGalServiceMixed;
    private final PyGalServiceValueAPIDynamic pyGalServiceValueAPIDynamic;

    public DemoController(PyGalServicePurePython pyGalServicePurePython, PyGalServicePureJava pyGalServicePureJava, PyGalServiceMixed pyGalServiceMixed, PyGalServiceValueAPIDynamic pyGalServiceValueAPIDynamic) {
        this.pyGalServicePurePython = pyGalServicePurePython;
        this.pyGalServicePureJava = pyGalServicePureJava;
        this.pyGalServiceMixed = pyGalServiceMixed;
        this.pyGalServiceValueAPIDynamic = pyGalServiceValueAPIDynamic;
    }

    @GetMapping(value = "/python", produces = "image/svg+xml")
    public String renderXYChartPurePython() {
        return pyGalServicePurePython.renderXYChart();
    }

    @GetMapping(value = "/java", produces = "image/svg+xml")
    public String renderXYChartPureJava() {
        return pyGalServicePureJava.renderXYChart();
    }

    @GetMapping(value = "/mixed", produces = "image/svg+xml")
    public String renderXYChartMixed() {
        return pyGalServiceMixed.renderXYChart();
    }

    @GetMapping(value = "/dynamic", produces = "image/svg+xml")
    public String renderXYChartValueAPIDynamic() {
        return pyGalServiceValueAPIDynamic.renderXYChart();
    }
}
