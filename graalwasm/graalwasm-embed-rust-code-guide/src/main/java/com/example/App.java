package com.example;

import com.sun.tools.javac.Main;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("js","wasm")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.webassembly", "true")
                .option("js.text-encoding","true").build();
        Path jsFilePath = Paths.get("target", "main.js");
        Source jsSource = Source.newBuilder("js", jsFilePath.toFile()).mimeType("application/javascript+module").build();
        context.eval(jsSource);
    }
}
