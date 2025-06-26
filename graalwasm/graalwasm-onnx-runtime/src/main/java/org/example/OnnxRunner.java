package org.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OnnxRunner {
    private final Context context;

    public OnnxRunner() throws IOException {

        context = Context.newBuilder("js", "wasm")
                .options(Main.getLanguageOptions())
                .allowAllAccess(true)
                .build();

        byte[] wasmBinary = Files.readAllBytes(Paths.get("src/main/resources/ort-wasm.wasm"));
        context.getBindings("js").putMember("modelWasmBuffer", wasmBinary);

        context.eval("js", """
                if (typeof performance === 'undefined') {
                  globalThis.performance = {
                    now: () => Date.now()
                  };
                }
                globalThis.self = globalThis;
                """);

        context.eval(Source.newBuilder("js", Objects.requireNonNull(Main.class.getResource("/ort.js"))).build());
        context.eval(Source.newBuilder("js", Objects.requireNonNull(Main.class.getResource("/script.js"))).build());
    }

    public void predict(byte[] modelData, Blackhole blackhole) {
        Main.GenerateFunction genratedFunction = context.getBindings("js").getMember("predict").as(Main.GenerateFunction.class);
        Main.Prediction prediction = genratedFunction.apply(modelData);
        prediction.then(result -> {
            blackhole.consume(result[0].getArrayElement(0).asInt());
            return null;
        });}
}
