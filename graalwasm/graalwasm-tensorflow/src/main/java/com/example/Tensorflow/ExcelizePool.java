package com.example.Tensorflow;

import org.graalvm.polyglot.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component

public class ExcelizePool {
    private final Context context;

    public ExcelizePool() throws IOException {

        // Use regular file paths
        byte[] excelizeWasmBytes = Files.readAllBytes(Paths.get("src/main/resources/excelize.wasm"));
        String test = Files.readString(Paths.get("src/main/resources/excelize.js"), StandardCharsets.UTF_8);
        String prep = Files.readString(Paths.get("src/main/resources/excelize_prep.js"), StandardCharsets.UTF_8);
         String excelizeLib = Files.readString(Paths.get("src/main/resources/excelize_m.js"), StandardCharsets.UTF_8);

        System.out.println("Executing excelize read...");

        // Configure engine options
        Map<String, String> options = new HashMap<>();
        options.put("js.webassembly", "true");
        options.put("js.text-encoding","true");

        Map<String, String> engineOptions = new HashMap<>();
        //engineOptions.put("engine.CompilerThreads", "1");
        engineOptions.put("engine.MultiTier", "true");
        engineOptions.put("engine.Mode", "throughput");

        Engine engine = Engine.newBuilder("js", "wasm")
                .allowExperimentalOptions(true)
                .options(engineOptions)
                .build();

        // Build the context
        Context context = Context.newBuilder("js", "wasm")
                .engine(engine)
                .allowAllAccess(true)
                .options(options)
                .build();


        context.eval(Source.newBuilder("js", prep, "prep.js").build());

        // Evaluate the Excelize WASM module
        Source excelizeModule = Source.newBuilder("js", excelizeLib, "excelize.mjs")
                .mimeType("application/javascript+module")
                .uri(URI.create("excelize.mjs"))
                .build();
        Value excelizeMod = context.eval(excelizeModule);
        context.getPolyglotBindings().putMember("excelize", excelizeMod);
        context.getBindings("js").putMember("wasmBytes", excelizeWasmBytes);

        // Evaluate test script
        context.eval(Source.newBuilder("js", test, "excelize.js").build());

        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
