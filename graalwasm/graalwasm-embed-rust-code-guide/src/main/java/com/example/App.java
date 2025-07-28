/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;

public class App {
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("wasm").option("wasm.Builtins", "wasi_snapshot_preview1").build();
        URL wasmFile = App.class.getResource("/hello_rust.wasm");
        Source source = Source.newBuilder("wasm", wasmFile).build();
        Value wasmBindings = context.eval(source);
        Value add = wasmBindings.getMember("add");

        int result = add.execute(5, 7).asInt();
        System.out.println("5 + 7 = " + result);
    }
}