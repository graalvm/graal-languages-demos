/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import java.io.IOException;
import java.net.URL;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class App {
    public static void main(String[] args) throws IOException {
        // Find the WebAssembly module resource
        URL wasmFile = App.class.getResource("floyd.wasm");
        Source source = Source.newBuilder("wasm", wasmFile).name("example").build();

        // Create Wasm context
        try (Context context = Context.newBuilder("wasm").option("wasm.Builtins", "wasi_snapshot_preview1").build()) {
            // Compile and instantiate the module
            Value module = context.eval(source);
            Value instance = module.newInstance();

            // Get the exports member from the module instance
            Value exports = instance.getMember("exports");

            // Invoke an exported functions
            exports.invokeMember("_initialize");
            exports.invokeMember("floyd", 10);

            // Or if you need to call a function multiple times
            Value floyd = exports.getMember("floyd");
            floyd.execute(4);
            floyd.execute(8);
        }
    }
}
