/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
    @Test
    void appRunsAndCallsGuestJava() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            App.main(new String[]{"Duke"});
        } finally {
            System.setOut(originalOut);
        }

        String output = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Hello from guest Java, Duke!"));
        assertTrue(output.contains("19 + 23 = 42"));
    }
}
