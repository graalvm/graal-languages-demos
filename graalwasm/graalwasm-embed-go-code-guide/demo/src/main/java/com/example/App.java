package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) throws IOException, URISyntaxException {
        try (Context context = Context.newBuilder("js", "wasm")
                .option("js.webassembly", "true")
                .option("js.commonjs-require", "true")
                .option("js.text-encoding", "true")
                .option("js.performance", "true")
                .option("js.global-property", "true")
                .allowAllAccess(true)
                .build()){
            byte[] wasmData = Files.readAllBytes(Path.of(App.class.getResource("/go/main.wasm").toURI()));
            context.getBindings("js").putMember("wasmData", wasmData);
            context.eval(Source.newBuilder("js", App.class.getResource("/polyfills.js")).build());
            context.eval(Source.newBuilder("js", App.class.getResource("/go/wasm_exec.js")).build());
            context.eval(Source.newBuilder("js", App.class.getResource("/main.js")).build());
        }
    }
}