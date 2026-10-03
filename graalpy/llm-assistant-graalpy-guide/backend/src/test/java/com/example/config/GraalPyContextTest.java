package com.example.config;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments = "test")
class GraalPyContextTest {

    @Inject
    GraalPyContext graalPyContext;

    @Test
    void contextShouldBeInitialized() {
        Context context = graalPyContext.getContext();
        assertNotNull(context, "Context must not be null");
        assertNotNull(context.getEngine().getLanguages().get("python"));
    }

    @AfterAll
    static void cleanup(GraalPyContext graalPyContext) {
        assertDoesNotThrow(graalPyContext::close);
    }
}
