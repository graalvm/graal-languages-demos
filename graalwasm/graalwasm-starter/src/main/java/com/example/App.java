/*
 * Copyright (c) 2024, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import java.io.IOException;
import java.net.URL;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class App {

    public static void main(String[] args) throws IOException {
        try (Context context = Context.create()) {
            URL wasmFile = App.class.getResource("add-two.wasm");
            Value mainModule = context.eval(Source.newBuilder("wasm", wasmFile).build());
            Value mainInstance = mainModule.newInstance();
            Value addTwo = mainInstance.getMember("exports").getMember("addTwo");
            System.out.println("addTwo(40, 2) = " + addTwo.execute(40, 2));
        }
    }
}
