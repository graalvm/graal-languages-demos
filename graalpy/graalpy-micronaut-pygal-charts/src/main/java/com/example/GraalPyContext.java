/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PreDestroy;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;

@Context
public class GraalPyContext {
    private final org.graalvm.polyglot.Context context = GraalPyResources.createContext();

    public GraalPyContext() {
        context.initialize("python");
    }

    public Value eval(String source) {
        return context.eval("python", source);
    }

    @PreDestroy
    void close() {
        context.close(true);
    }
}
