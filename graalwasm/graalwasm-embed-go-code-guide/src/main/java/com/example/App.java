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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {
    public static final String GO_MAIN_WASM = "/go/main.wasm";
    public static final String GO_WASM_EXEC = "/go/wasm_exec.js";

    public static void main(String[] args) throws IOException, URISyntaxException {
        URL mainWasmURL = getResource(GO_MAIN_WASM);
        byte[] wasmBytes = Files.readAllBytes(Path.of(mainWasmURL.toURI()));
        URL wasmExecURL = getResource(GO_WASM_EXEC);
        try (Context context = Context.newBuilder("js", "wasm")
                .option("js.performance", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true")
                .allowAllAccess(true)
                .build()) {
            Value jsBindings = context.getBindings("js");
            jsBindings.putMember("wasmBytes", wasmBytes);
            jsBindings.putMember("crypto", new CryptoPolyfill());
            context.eval(Source.newBuilder("js", wasmExecURL).build());
            context.eval("js", """
                    async function main(wasmBytes) {
                        const go = new Go();
                        const {instance} = await WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject);
                        go.run(instance);
                    }
                    main(wasmBytes);
                    """);
            GoMain goMain = jsBindings.getMember("main").as(GoMain.class);
            System.out.printf("3 + 4 = %s%n", goMain.add(3, 4));
            System.out.printf("reverseString('Hello World') = %s%n", goMain.reverseString("Hello World"));
        }
    }

    private static URL getResource(String name) throws FileNotFoundException {
        URL url = App.class.getResource(name);
        if (url == null) {
            throw new FileNotFoundException(GO_MAIN_WASM);
        }
        return url;
    }
}