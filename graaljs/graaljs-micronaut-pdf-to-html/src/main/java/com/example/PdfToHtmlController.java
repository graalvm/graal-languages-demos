/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.example;

import io.micronaut.http.annotation.*;
import io.micronaut.http.MediaType;

@Controller("/")
public class PdfToHtmlController {

    private final PdfToHtmlService pdfService;

    public PdfToHtmlController(PdfToHtmlService pdfService) {
        this.pdfService = pdfService;
    }

    @Get(produces = MediaType.TEXT_HTML)
    public String getHtmlFromPdf() throws Exception {
        return pdfService.convert();
    }
}
