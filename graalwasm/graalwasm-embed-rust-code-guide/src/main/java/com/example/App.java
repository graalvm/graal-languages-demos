/*
 * Copyright (c) 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;


public class App {
    private static final String MYWASMLIB_JS_RESOURCE = "/mywasmlib/mywasmlib.js";

    public static void main(String[] args) throws IOException {
        URL myWasmLibURL = App.class.getResource(MYWASMLIB_JS_RESOURCE);
        if (myWasmLibURL == null) {
            throw new FileNotFoundException(MYWASMLIB_JS_RESOURCE);
        }
        try (Context context = Context.newBuilder("js", "wasm")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true").build()) {
            Source jsSource = Source.newBuilder("js", myWasmLibURL).mimeType("application/javascript+module").build();
            MyWasmLib myWasmLibModule = context.eval(jsSource).as(MyWasmLib.class);

            System.out.println(myWasmLibModule.add(2, 3));
            System.out.println(myWasmLibModule.new_person("Jane").say_hello());
            System.out.println(myWasmLibModule.reverse_string("Hello There!"));
        }
    }

    interface MyWasmLib {
        int add(int left, int right);

        interface Person {
            String say_hello();
        }

        Person new_person(String name);

        String reverse_string(String word);
    }
}
