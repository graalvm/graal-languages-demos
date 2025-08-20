/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class RootViewTest {
    @Inject @Client("/")
    HttpClient client;

    @Test
    void rootViewRendersServerSide() {
        var html = client.toBlocking().exchange("/", String.class).body();

        // Check that title is correctly rendered
        assertTrue(html.contains("<title>Charts</title>"), html);

        // Check the embedded rootProps and rootComponent in the script tag
        assertTrue(html.contains("\"rootProps\":{\"title\":\"Charts\",\"url\":\"/\"}"), html);
        assertTrue(html.contains("\"rootComponent\":\"App\""), html);
    }

}
