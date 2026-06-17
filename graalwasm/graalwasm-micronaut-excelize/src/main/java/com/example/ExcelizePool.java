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

        // Read the WASM file bytes
        byte[] excelizeWasmBytes = resourceResolver.getResourceAsStream("classpath:excelize.wasm").get().readAllBytes();


        System.out.println("Executing excelize read...");

        String test = new String(resourceResolver.getResourceAsStream("classpath:excelize.js").get().readAllBytes(), StandardCharsets.UTF_8);
        String prep = new String(resourceResolver.getResourceAsStream("classpath:excelize_prep.js").get().readAllBytes(), StandardCharsets.UTF_8);
        String excelizeLib = new String(resourceResolver.getResourceAsStream("classpath:excelize_m.js").get().readAllBytes(), StandardCharsets.UTF_8);

        // Configure options (same as your write code)
        Map<String, String> options = new HashMap<>();
        //options.put("engine.CompilationFailureAction", "Diagnose");
        options.put("js.top-level-await", "true");
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        options.put("js.text-encoding","true");

        Map<String, String> engineOptions = new HashMap<>();
        engineOptions.put("engine.CompilerThreads", "1");
        engineOptions.put("engine.WarnInterpreterOnly", "false");
        engineOptions.put("engine.CompilationFailureAction", "Diagnose");
        engineOptions.put("engine.MultiTier", "true");
        engineOptions.put("engine.Mode", "throughput");
        int maxThreads = Runtime.getRuntime().availableProcessors();
        contexts = new LinkedBlockingQueue<>(maxThreads);
        Engine engine = Engine.newBuilder("js", "wasm")
                .allowExperimentalOptions(true)
                .options(engineOptions)
                .build();
        for (int i = 0; i < maxThreads; i++) {
            Context context = Context.newBuilder("js", "wasm")
                    .engine(engine)
                    .allowAllAccess(true)
                    .options(options)
                    .build();

            Source prepModule = Source.newBuilder("js", prep, "prep.js").build();
            context.eval(prepModule);

            // Load the excelize module as an ECMAScript module
            Source excelizeModule = Source.newBuilder("js", excelizeLib, "excelize.mjs")
                    .mimeType("application/javascript+module")
                    .build();
            Value excelizeMod = context.eval(excelizeModule);
            context.getPolyglotBindings().putMember("excelize", excelizeMod);
            context.getBindings("js").putMember("wasmBytes", excelizeWasmBytes);
            Source testRun = Source.newBuilder("js", test, "excelize.js").build();
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
