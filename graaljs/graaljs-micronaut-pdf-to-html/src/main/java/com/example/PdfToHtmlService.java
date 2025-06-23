/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.example;

import jakarta.inject.Singleton;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Singleton
public class PdfToHtmlService {

    public static final String PDF_LOCATION = "/PDFHTML.pdf";
    public static final String RESOURCES_POLYFILL_JS = "/polyfill.js";
    public static final String BUNDLE_JS = "/dist/pdf-to-html.bundle.js";

    public String convert() throws Exception {
        byte[] pdfData = readPdfFile();
        Source jsPolyfill = loadJsPolyfill();
        Source bundleSource = loadBundleJsSource();
        return convertPdfToHtml(pdfData, jsPolyfill, bundleSource);
    }

    private byte[] readPdfFile() throws Exception {
        URL pdfURL = getClass().getResource(PDF_LOCATION);
        if (pdfURL == null) throw new RuntimeException("PDF not found: " + PDF_LOCATION);
        return Files.readAllBytes(Paths.get(pdfURL.toURI()));
    }

    private Source loadJsPolyfill() throws Exception {
        URL polyfillURL = getClass().getResource(RESOURCES_POLYFILL_JS);
        if (polyfillURL == null) throw new RuntimeException("Polyfill not found: " + RESOURCES_POLYFILL_JS);
        return Source.newBuilder("js", polyfillURL).build();
    }

    private Source loadBundleJsSource() throws Exception {
        URL bundleURL = getClass().getResource(BUNDLE_JS);
        if (bundleURL == null) throw new RuntimeException("Bundle JS not found: " + BUNDLE_JS);
        return Source.newBuilder("js", bundleURL).build();
    }

    private String convertPdfToHtml(byte[] pdfData, Source jsPolyfill, Source bundleSource) throws Exception {
        try (Context context = Context.newBuilder("js")
                .allowIO(IOAccess.newBuilder().allowHostFileAccess(true).build())
                .allowHostAccess(HostAccess.ALL)
                .build()) {

            context.eval(jsPolyfill);
            context.eval(bundleSource);
            Value pdfToHtmlFunc = context.getBindings("js").getMember("pdfToHtml");
            if (pdfToHtmlFunc == null || !pdfToHtmlFunc.canExecute()) {
                throw new RuntimeException("Function pdfToHtml not found or not executable");
            }
            Value promise = pdfToHtmlFunc.execute(pdfData);
            return promiseToFuture(promise).get();
        }
    }

    private static CompletableFuture<String> promiseToFuture(Value promise) {
        CompletableFuture<String> future = new CompletableFuture<>();

        promise.invokeMember("then",
                (ProxyExecutable) args -> {
                    future.complete(args[0].asString());
                    return null;
                },
                (ProxyExecutable) args -> {
                    future.completeExceptionally(new RuntimeException(args[0].toString()));
                    return null;
                });
        return future;
    }
}
