package org.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import java.util.Map;
import java.util.Objects;


public class Main {
    public static Map<String, String> getLanguageOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("engine.CompilationFailureAction", "Diagnose");
        options.put("js.webassembly", "true");
        options.put("js.commonjs-require", "true");
        return options;
    }
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("js","wasm")
                .options(getLanguageOptions())
                .allowAllAccess(true)
                .build();
        byte[] wasmBinary  = Files.readAllBytes(Paths.get("src/main/resources/ort-wasm.wasm"));
        context.getBindings("js").putMember("modelWasmBuffer",wasmBinary);
        context.eval("js","""
                if (typeof performance === 'undefined') {
                  globalThis.performance = {
                    now: () => Date.now()
                  };
                }
                globalThis.self = globalThis;
                """);
        context.eval(Source.newBuilder("js", Objects.requireNonNull(Main.class.getResource("/ort.js")))
                .build());
        byte[] modelData = Files.readAllBytes(Paths.get("src/main/resources/modeel.onnx"));

        context.eval(Source.newBuilder("js", Objects.requireNonNull(Main.class.getResource("/script.js")))
                .build());
        int [] DataX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        int [] DataY = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};

        GenerateFunction genratedFunction = context.getBindings("js").getMember("predict").as(GenerateFunction.class);
        Prediction prediction = genratedFunction.apply(modelData, DataX , DataY);
        prediction.then(result -> {

            System.out.println("results from java side : "+result[0].getArrayElement(0).asInt());
            return null;
        });
    }

    @FunctionalInterface
    public interface GenerateFunction {
        Prediction apply(byte[] model , int [] DataX , int [] DataY);
    }

    public interface Prediction {
        void then(ProxyExecutable callback);
    }
}