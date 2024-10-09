/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.io.IOAccess;

public class App {

    public static void main(String[] args) {
        try (Context context = Context.newBuilder("python")
                         /* Enabling some of these is needed for various standard library modules */
                        .allowNativeAccess(false)
                        .allowCreateThread(false)
                        .allowIO(IOAccess.newBuilder()
                                        .allowHostFileAccess(false)
                                        .allowHostSocketAccess(false)
                                        .build())
                        .build()) {
            context.eval("python", "print('Hello from GraalPy!')");
        }
    }
}
