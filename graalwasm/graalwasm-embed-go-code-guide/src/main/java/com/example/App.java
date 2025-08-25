/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class App {
    public static final String GO_MAIN_WASM = "/go/main.wasm";
    public static final String GO_WASM_EXEC = "/go/wasm_exec.js";

    public static void main(String[] args) throws IOException {
        // Load Go resources
        byte[] wasmBytes;
        try (InputStream in = App.class.getResourceAsStream(GO_MAIN_WASM)) {
            if (in == null) {
                throw new FileNotFoundException(GO_MAIN_WASM);
            }
            wasmBytes = in.readAllBytes();
        }
        URL wasmExecURL = App.class.getResource(GO_WASM_EXEC);
        if (wasmExecURL == null) {
            throw new FileNotFoundException(GO_WASM_EXEC);
        }
        // Create a context with Wasm and JavaScript access
        try (Context context = Context.newBuilder("js", "wasm")
                .option("js.performance", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true")
                .allowAllAccess(true)
                .build()) {
            // Install Wasm bytes and crypto polyfill in JS binding
            Value jsBindings = context.getBindings("js");
            jsBindings.putMember("wasmBytes", wasmBytes);
            jsBindings.putMember("crypto", new CryptoPolyfill());
            // Evaluate wasm_exec.js file
            context.eval(Source.newBuilder("js", wasmExecURL).build());
            // Instantiate the Wasm module and invoke go.run()
            context.eval("js", """
                    async function run(wasmBytes) {
                        const go = new Go();
                        const {instance} = await WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject);
                        go.run(instance);
                    }
                    run(wasmBytes);
                    """);
            // Access main module and interact with it through a Java interface
            GoMain goMain = jsBindings.getMember("main").as(GoMain.class);
            System.out.printf("3 + 4 = %s%n", goMain.add(3, 4));
            System.out.printf("reverseString('Hello World') = %s%n", goMain.reverseString("Hello World"));
        }
    }
}