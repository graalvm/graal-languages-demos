/*
 * Copyright (c) 2025, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */
package com.example;

import org.graalvm.polyglot.Value;

import java.security.SecureRandom;

public class CryptoPolyfill {
    private final SecureRandom random = new SecureRandom();

    public Object getRandomValues(Value buffer) {
        if (!buffer.hasArrayElements()) {
            throw new IllegalArgumentException("TypeMismatchError: The data argument must be an integer-type TypedArray");
        }
        long arraySize = buffer.getArraySize();
        if (arraySize > 65536) {
            throw new IllegalArgumentException("QuotaExceededError: The requested length exceeds 65,536 bytes");
        }
        int size = Math.toIntExact(arraySize);
        byte[] randomBytes = new byte[size];
        random.nextBytes(randomBytes);
        for (int i = 0; i < size; i++) {
            buffer.setArrayElement(i, randomBytes[i]);
        }
        return buffer;
    }
}
