package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;

public class App {
    public static void main(String[] args)  {
        try (Context context = Context.newBuilder("wasm")
                .option("engine.WarnInterpreterOnly", "false")
                .option("wasm.Builtins", "wasi_snapshot_preview1")
                .allowAllAccess(true)
                .build()) {
        URL wasmFile = App.class.getResource("/main.wasm");
        Source source = Source.newBuilder("wasm",wasmFile).build();
        Value wasmBindings = context.eval( source);

        Value main = wasmBindings.getMember("foo");
        main.execute();
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
    }
}