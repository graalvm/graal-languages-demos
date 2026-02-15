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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class ReactViewTest {
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void rootViewRendersServerSide() {
        var html = client.toBlocking().exchange("/", String.class).body();

        // Check that title is correctly rendered
        assertTrue(html.contains("<title>Recharts Examples</title>"), "Title not set correctly");

        // Check the embedded rootProps and rootComponent in the script tag
        assertTrue(html.contains("\"rootProps\":{\"width\":500,\"title\":\"Recharts Examples\",\"height\":300,\"url\":\"/\"}"), "rootProps not set correctly");
        assertTrue(html.contains("\"rootComponent\":\"App\""), "rootComponent not set correctly");

        // Check SVG elements
        assertEquals(11, countOccurrences(html, "<svg"), "Number of SVG elements incorrect");
    }

    private static int countOccurrences(String str, String subStr) {
        int count = 0;
        int index = 0;

        while ((index = str.indexOf(subStr, index)) != -1) {
            count++;
            index += subStr.length(); // Move past the last found substring
        }

        return count;
    }
}
