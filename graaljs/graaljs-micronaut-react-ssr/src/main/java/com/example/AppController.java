/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;


@Controller
public class AppController {
    @Introspected
    public record InitialProps(String title, int width, int height) {
    }

    @View("App")
    @Get
    public HttpResponse<?> index() {
        return HttpResponse.ok(new InitialProps("Recharts Examples", 500, 300));
    }
}