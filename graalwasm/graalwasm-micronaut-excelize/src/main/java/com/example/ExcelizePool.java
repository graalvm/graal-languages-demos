/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.core.io.ResourceResolver;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@io.micronaut.context.annotation.Context
public class ExcelizePool {
    private final BlockingQueue<Context> contexts;


    public ExcelizePool(ResourceResolver resourceResolver) throws IOException {
        final String INTERNAL_MODULE_URI_HEADER = "oracle:/mle/";

        // Read the WASM file bytes
        byte[] excelizeWasmBytes = resourceResolver.getResourceAsStream("classpath:excelize.wasm").get().readAllBytes();


        System.out.println("Executing excelize read...");

        String test = new String(resourceResolver.getResourceAsStream("classpath:excelize_test.js").get().readAllBytes(), StandardCharsets.UTF_8);
        String prep = new String(resourceResolver.getResourceAsStream("classpath:excelize_prep.js").get().readAllBytes(), StandardCharsets.UTF_8);
        String encodingIdxs = new String(resourceResolver.getResourceAsStream("classpath:encoding-indexes.js").get().readAllBytes(), StandardCharsets.UTF_8);
        String encoding = new String(resourceResolver.getResourceAsStream("classpath:encoding.js").get().readAllBytes(), StandardCharsets.UTF_8);
        String excelizeLib = new String(resourceResolver.getResourceAsStream("classpath:excelize_m.js").get().readAllBytes(), StandardCharsets.UTF_8);

        // Configure options (same as your write code)
        Map<String, String> options = new HashMap<>();
        options.put("js.ecmascript-version", "2023");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        options.put("js.esm-eval-returns-exports", "true");
        options.put("js.unhandled-rejections", "throw");
        options.put("js.commonjs-require-cwd", Paths.get("./").toAbsolutePath().toString());

        Map<String, String> engineOptions = new HashMap<>();
        engineOptions.put("engine.CompilerThreads", "1");
        engineOptions.put("engine.WarnInterpreterOnly", "false");
        engineOptions.put("engine.MultiTier", "true");
        engineOptions.put("engine.Mode", "throughput");
        int maxThreads = Runtime.getRuntime().availableProcessors();
        contexts = new LinkedBlockingQueue<>(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
      Engine engine = Engine.newBuilder("js", "wasm")
                .allowExperimentalOptions(true)
                .options(engineOptions)
                .build();



            // Create context for executing the read function
            Context context = Context.newBuilder("js", "wasm")
                    .engine(engine)
                    .allowIO(IOAccess.ALL)
                    .allowAllAccess(true)
                    .allowPolyglotAccess(PolyglotAccess.ALL)
                    .allowExperimentalOptions(true)
                    .allowHostClassLookup(s -> true)
                    .allowHostAccess(HostAccess.ALL)
                    .options(options)
                    .build();

            // Evaluate additional helper modules
            Source encodingIdxsModule = Source.newBuilder("js", encodingIdxs, "encoding-indexes.js").build();
            context.eval(encodingIdxsModule);
            Source encodingModule = Source.newBuilder("js", encoding, "encoding.js").build();
            context.eval(encodingModule);
            Source prepModule = Source.newBuilder("js", prep, "prep.js").build();
            context.eval(prepModule);

            // Load the excelize module as an ECMAScript module
            Source excelizeModule = Source.newBuilder("js", excelizeLib, "excelize.mjs")
                    .mimeType("application/javascript+module")
                    .uri(URI.create(INTERNAL_MODULE_URI_HEADER + "excelize.mjs"))
                    .build();
            Value excelizeMod = context.eval(excelizeModule);
            context.getPolyglotBindings().putMember("excelize", excelizeMod);
            context.getBindings("js").putMember("wasmBytes", excelizeWasmBytes);
            Source testRun = Source.newBuilder("js", test, "excelize_test.js").build();
            context.eval(testRun);

            this.contexts.add(context);
        }
    }



    public Context getContext() {
        try {
            return contexts.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    void release(Context context) {
        contexts.add(context);
    }
}
