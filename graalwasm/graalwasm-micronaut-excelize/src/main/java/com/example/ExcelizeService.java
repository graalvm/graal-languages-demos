package com.example;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@io.micronaut.context.annotation.Context
public class ExcelizeService {

    private final ResourceResolver resourceResolver;

    public ExcelizeService(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }


    public void runExcelizeComplete(Object[][] array) throws IOException {
        // Constants
        final String INTERNAL_MODULE_URI_HEADER = "oracle:/mle/";

        // Helper method for reading files
        byte[] excelizeWasmBytes  = resourceResolver.getResourceAsStream("classpath:excelize.wasm").get().readAllBytes();

        // Load test file based on fileSize
        String test = null;
        System.out.println("Executing excelize ");

        test = Files.readString(Paths.get("./src/main/resources/excelize_test.js"));
        // Load required JavaScript files

        String prep = Files.readString(Paths.get("./src/main/resources/excelize_prep.js"));
        String encodingIdxs = Files.readString(Paths.get("./src/main/resources/encoding-indexes.js"));
        String encoding = Files.readString(Paths.get("./src/main/resources/encoding.js"));
        String excelizeLib = Files.readString(Paths.get("./src/main/resources/excelize_m.js"));

        // Configure options
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
        engineOptions.put("engine.TraceCompilationDetails", "true");
        engineOptions.put("engine.WarnInterpreterOnly", "false");
        engineOptions.put("engine.MultiTier", "true");
        engineOptions.put("engine.Mode", "throughput");
        engineOptions.put("log.file", "test1.log");

        // Create and configure the engine
        try (Engine engine = Engine.newBuilder("js", "wasm")
                .allowExperimentalOptions(true)
                .options(engineOptions)
                .build()) {

            // Initialize context for WASM precompilation


            if (test != null) {
                // For each iteration, create a new context
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

                Source encodingIdxsModule = Source.newBuilder("js", encodingIdxs, "encoding-indexes.js").build();
                context.eval(encodingIdxsModule);
                Source encodingModule = Source.newBuilder("js", encoding, "encoding.js").build();
                context.eval(encodingModule);

                Source prepModule = Source.newBuilder("js", prep, "prep.js").build();
                context.eval(prepModule);


                Source excelizeModule = Source.newBuilder("js", excelizeLib, "excelize.mjs")
                        .mimeType("application/javascript+module")
                        .uri(URI.create(INTERNAL_MODULE_URI_HEADER + "excelize.mjs"))
                        .build();
                Value excelizeMod = context.eval(excelizeModule);
                context.getPolyglotBindings().putMember("excelize", excelizeMod);
                context.getBindings("js").putMember("wasmBytes", excelizeWasmBytes);

                // Run the test
                Source testRun = Source.newBuilder("js", test, "excelize_test.js").build();
                context.eval(testRun);
                Value x = context.getBindings("js").getMember("generateExcel");


                Value jsArray = context.eval("js", "[]");
                for (Object[] row : array) {
                    Value jsRow = context.eval("js", "[]");
                    for (Object cell : row) {
                        jsRow.setArrayElement(jsRow.getArraySize(), cell == null ? Value.asValue((Object) null) : cell);
                    }
                    jsArray.setArrayElement(jsArray.getArraySize(), jsRow);
                }

                x.execute(jsArray);

                // Save output Excel file
                Value buffer = context.getPolyglotBindings().getMember("excelBuffer");
                if (buffer != null && buffer.hasArrayElements()) {
                    int length = (int) buffer.getArraySize();
                    byte[] fileBytes = new byte[length];
                    for (int j = 0; j < length; j++) {
                        fileBytes[j] = (byte) buffer.getArrayElement(j).asInt();
                    }
                    Files.write(Paths.get("output.xlsx"), fileBytes);
                    System.out.println("Excel file saved as output.xlsx");
                } else {
                    System.err.println("No buffer exported from JS.");
                }

                // Close the context
                context.close();
            }

        }
    }

    public List<Book> readExcelFromFile(byte[] excelBytes) throws IOException {
        final String INTERNAL_MODULE_URI_HEADER = "oracle:/mle/";

        // Read the WASM file bytes
        byte[] excelizeWasmBytes = resourceResolver.getResourceAsStream("classpath:excelize.wasm").get().readAllBytes();


        System.out.println("Executing excelize read...");

        // Load required JavaScript files:
        String prep = Files.readString(Paths.get("./src/main/resources/excelize_prep.js"));
        String encodingIdxs = Files.readString(Paths.get("./src/main/resources/encoding-indexes.js"));
        String encoding = Files.readString(Paths.get("./src/main/resources/encoding.js"));
        String excelizeLib = Files.readString(Paths.get("./src/main/resources/excelize_m.js"));
        // New JS file which defines our reading function (readExcel)
        String test = Files.readString(Paths.get("./src/main/resources/excelize_test.js"));

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
        engineOptions.put("engine.TraceCompilationDetails", "true");
        engineOptions.put("engine.WarnInterpreterOnly", "false");
        engineOptions.put("engine.MultiTier", "true");
        engineOptions.put("engine.Mode", "throughput");
        engineOptions.put("log.file", "test1.log");

        try (Engine engine = Engine.newBuilder("js", "wasm")
                .allowExperimentalOptions(true)
                .options(engineOptions)
                .build()) {



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

            // Read the existing Excel file ("output.xlsx") from disk
            byte[] fileBytes = Files.readAllBytes(Paths.get("output.xlsx"));
            // Convert file bytes to a JS array
            Value jsArray = context.eval("js", "[]");
            for (byte b : fileBytes) {
                // Ensure the byte is passed as a number (0..255)
                jsArray.setArrayElement(jsArray.getArraySize(), b & 0xFF);
            }
            // Expose the file bytes to the JS context via polyglot binding "excelFile"
            context.getPolyglotBindings().putMember("excelFile", jsArray);

            context.getBindings("js").putMember("excelFileBytes", excelBytes);
            Source testRun = Source.newBuilder("js", test, "excelize_test.js").build();
            context.eval(testRun);

            Value readFunc = context.getBindings("js").getMember("readExcel");readFunc.execute();
            Value bufferArray = context.getPolyglotBindings().getMember("resultArray");



            List<Book> books = new ArrayList<>();


            if (bufferArray.hasArrayElements()) {
                for (int i = 1; i < bufferArray.getArraySize(); i++) {
                    Value row = bufferArray.getArrayElement(i);
                    if (row.hasArrayElements()) {

                        String id = row.getArrayElement(0).asString();
                        String author = row.getArrayElement(1).asString();
                        String title = row.getArrayElement(2).asString();
                        System.out.println(id);
                        // Create a new Book object and add it to the list
                        books.add(new Book(id,author, title));
                    }
                }
            }
            return books;



        }
        }
    }
