package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("js","wasm")
                .option("js.webassembly", "true")
                .option("js.commonjs-require", "true")
                .allowAllAccess(true)
                .option("js.text-encoding","true").build();
        byte [] wasmData = Files.readAllBytes(Path.of("src/main/resources/main.wasm"));
        context.getBindings("js").putMember("wasmData",wasmData);
        context.eval(Source.newBuilder("js",App.class.getResource("/wasm_exec.js")).build());
        context.eval(Source.newBuilder("js",App.class.getResource("/main.js")).build());

    }
}