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

        // Setup context
        Context.Builder contextBuilder = Context.newBuilder("wasm").option("wasm.Builtins", "wasi_snapshot_preview1");
        Source.Builder sourceBuilder = Source.newBuilder("wasm", wasmFile).name("example");
        Source source = sourceBuilder.build();
        Context context = contextBuilder.build();

        // Evaluate the WebAssembly module
        context.eval(source);

        // Execute the floyd function
        context.getBindings("wasm").getMember("example").getMember("_initialize").executeVoid();
        Value mainFunction = context.getBindings("wasm").getMember("example").getMember("floyd");
        mainFunction.execute();
        context.close();
    }
}
