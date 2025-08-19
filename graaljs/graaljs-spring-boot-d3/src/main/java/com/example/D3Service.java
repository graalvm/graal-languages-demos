/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class D3Service {
    private final Engine sharedEngine = Engine.create();
    private final BlockingQueue<RenderChordFunction> renderChordFunctionPool;

    @FunctionalInterface
    public interface RenderChordFunction {
        String apply(int width, int height);
    }

    public D3Service() throws IOException {
        Source d3ChordBundleSource = Source.newBuilder("js", new ClassPathResource("js/d3-chord.bundle.js").getURL())
                .mimeType("application/javascript+module").build();
        int maxThreads = Runtime.getRuntime().availableProcessors();
        renderChordFunctionPool = new LinkedBlockingQueue<>(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            Context context = createContext();
            context.eval(d3ChordBundleSource);
            RenderChordFunction renderChordFunction =
                    context.getBindings("js").getMember("renderChord").as(RenderChordFunction.class);
            renderChordFunctionPool.add(renderChordFunction);
        }
    }

    public String renderChord(Model model) {
        RenderChordFunction renderChordFunction = null;
        try {
            renderChordFunction = renderChordFunctionPool.take();
            model.addAttribute("svgContent", renderChordFunction.apply(640, 640));
            return "d3-chord";
        } catch (PolyglotException | InterruptedException e) {
            model.addAttribute("errorMessage", "Error generating the diagram.");
            model.addAttribute("errorDetails", e.getMessage());
            return "error";
        } finally {
            if (renderChordFunction != null) {
                renderChordFunctionPool.add(renderChordFunction);
            }
        }
    }

    private Context createContext() {
        return Context.newBuilder("js")
                .engine(sharedEngine)
                .allowIO(IOAccess.newBuilder().allowHostFileAccess(true).build())
                .build();
    }
}
