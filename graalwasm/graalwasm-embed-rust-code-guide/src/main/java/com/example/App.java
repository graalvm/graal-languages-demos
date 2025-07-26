package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;

public class App {
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("wasm")
                .option("wasm.Builtins", "wasi_snapshot_preview1")
                .build();
        URL wasmFile = App.class.getResource("/hello-rust.wasm");
        Source source = Source.newBuilder("wasm",wasmFile).build();
        Value wasmBindings = context.eval( source);

        Value main = wasmBindings.getMember("_start");
        main.execute();
    }
}