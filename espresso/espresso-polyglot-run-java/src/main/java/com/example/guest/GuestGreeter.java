/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example.guest;

public final class GuestGreeter {
    private final String prefix;

    public GuestGreeter(String prefix) {
        this.prefix = prefix;
    }

    public String greet(String name) {
        return prefix + " from guest Java, " + name + "!";
    }

    public static int add(int left, int right) {
        return left + right;
    }
}
