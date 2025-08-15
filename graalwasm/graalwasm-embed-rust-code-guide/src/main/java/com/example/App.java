/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


public class App {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Context context = Context.newBuilder("js", "wasm")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.webassembly", "true")
                .option("js.text-encoding", "true").build();
        URL myWasmLibURL = App.class.getResource("/mywasmlib/mywasmlib.js");
        Source jsSource = Source.newBuilder("js", myWasmLibURL).mimeType("application/javascript+module").build();
        MyWasmLib myWasmLibModule = context.eval(jsSource).as(MyWasmLib.class);
        System.out.println(myWasmLibModule.add(2, 3));
        System.out.println(myWasmLibModule.new_person("Anwar").say_hello());
        System.out.println(myWasmLibModule.reverse_string("Hello There!"));
    }

    interface MyWasmLib {
        int add(int left, int right);

        Person new_person(String name);

        interface Person {
            String say_hello();
        }
        String  reverse_string (String word);
    }
}
