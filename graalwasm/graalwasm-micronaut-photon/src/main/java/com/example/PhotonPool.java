/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import com.example.Photon.PhotonImage;
import com.example.Photon.Uint8Array;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.io.ResourceResolver;
import jakarta.annotation.PreDestroy;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Context
public class PhotonPool {
    private final Engine sharedEngine = Engine.newBuilder()
            .option("dap", System.getProperty("dap", "false"))
            .option("inspect", System.getProperty("inspect", "false"))
            .build();
    private final BlockingQueue<Photon> photons;

    PhotonPool(ResourceResolver resourceResolve) throws IOException {
        URL photonModuleURL = resourceResolve.getResource("classpath:photon/photon.js").get();
        Source photonSource = Source.newBuilder("js", photonModuleURL).mimeType("application/javascript+module").build();
        byte[] imageBytes = resourceResolve.getResourceAsStream("classpath:daisies_fuji.jpg").get().readAllBytes();

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
        var context = org.graalvm.polyglot.Context.newBuilder("js", "wasm")
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
}
