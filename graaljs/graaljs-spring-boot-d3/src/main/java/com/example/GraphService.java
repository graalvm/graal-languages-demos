package com.example;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GraphService {

    private final Engine sharedEngine = Engine.newBuilder().option("engine.TraceCompilation", "true").build();

    public String generateGraph() throws IOException {

        try (Context context = Context.newBuilder("js")
                .engine(sharedEngine)
                .allowIO(IOAccess.ALL)
                .allowExperimentalOptions(true)
                .option("js.commonjs-require", "true")
                .build()) {

            Source source = Source.newBuilder("js", new ClassPathResource("static/graph.js").getURL()).mimeType("application/javascript+module").build();
            Value result = context.eval(source);
            return result.asString();

        } catch (PolyglotException e) {
            System.err.println("JavaScript Error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error executing JavaScript code: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error executing JavaScript code", e);
        }
    }
}