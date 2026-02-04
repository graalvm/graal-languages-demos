/*
 * Copyright (c) 2024, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example.demo;

import com.example.demo.Photon.PhotonImage;
import com.example.demo.Photon.Uint8Array;
import jakarta.annotation.PreDestroy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@ImportRuntimeHints(PhotonPool.PhotonPoolRuntimeHints.class)
public class PhotonPool {
    private final Engine sharedEngine = Engine.newBuilder()
            .option("dap", System.getProperty("dap", "false"))
            .option("inspect", System.getProperty("inspect", "false"))
            .build();
    private final BlockingQueue<Photon> photons;

    PhotonPool() throws IOException {
        Source photonSource = Source.newBuilder("js", new ClassPathResource("photon/photon.js").getURL()).mimeType("application/javascript+module").build();
        byte[] imageBytes = new ClassPathResource("daisies_fuji.jpg").getContentAsByteArray();

        int maxThreads = Runtime.getRuntime().availableProcessors();
        photons = new LinkedBlockingQueue<>(maxThreads);
        for (int i = 0; i < maxThreads; i++) {
            photons.add(createPhoton(sharedEngine, photonSource, imageBytes));
        }
    }

    Photon take() {
        try {
            return photons.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void release(Photon context) {
        photons.add(context);
    }

    @PreDestroy
    public void close() {
        sharedEngine.close();
    }

    private static Photon createPhoton(Engine engine, Source photonSource, Object imageBytes) {
        Context context = Context.newBuilder("js", "wasm")
                .engine(engine)
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true")
                .build();

        // Load Photon module and initialize with wasm content
        Value photonModule = context.eval(photonSource);

        // Fetch PhotonImage class
        PhotonImage photonImage = photonModule.getMember("PhotonImage").as(PhotonImage.class);

        // Create Uint8Array with image bytes
        Uint8Array imageContent = context.getBindings("js").getMember("Uint8Array").newInstance(imageBytes).as(Uint8Array.class);

        return new Photon(photonModule, photonImage, imageContent);
    }

    static class PhotonPoolRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources()
                    .registerPattern("photon/*")
                    .registerPattern("daisies_fuji.jpg");
            hints.proxies()
                    .registerJdkProxy(PhotonImage.class)
                    .registerJdkProxy(Uint8Array.class);
        }
    }
}
