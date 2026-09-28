package com.example.config;

import io.micronaut.context.annotation.Context;
import jakarta.annotation.PreDestroy;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.python.embedding.GraalPyResources;
import java.io.IOException;

@Context // ①
public class GraalPyContext {

    public static final String PYTHON="python";
    private final org.graalvm.polyglot.Context context;

    public GraalPyContext() throws IOException {


        context= GraalPyResources.contextBuilder()
                .allowEnvironmentAccess(EnvironmentAccess.INHERIT) // ②
                .option("python.WarnExperimentalFeatures", "false") // ③
                .build();


        context.initialize(PYTHON); // ④
    }

    public org.graalvm.polyglot.Context getContext() {

        return context; // ⑤
    }

    @PreDestroy
    void close(){
        try {
            context.close(true); // ⑥
        } catch (Exception e) {
            //ignore
        }
    }
}