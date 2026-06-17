/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class App {
    private static final String GUEST_CLASS_NAME = "com.example.guest.GuestGreeter";

    public static void main(String[] args) throws URISyntaxException {
        String guestClasspath = resolveGuestClasspath();
        String name = args.length > 0 ? args[0] : "Espresso";

        try (Context context = Context.newBuilder("java")
                .allowAllAccess(true)
                .option("java.Classpath", guestClasspath)
                .build()) {
            Value guestGreeterClass = context.getBindings("java").getMember(GUEST_CLASS_NAME);
            Value guestGreeter = guestGreeterClass.newInstance("Hello");

            String greeting = guestGreeter.invokeMember("greet", name).asString();
            int sum = guestGreeterClass.invokeMember("add", 19, 23).asInt();

            System.out.println("Guest classpath: " + guestClasspath);
            System.out.println(greeting);
            System.out.println("19 + 23 = " + sum);
        }
    }

    private static String resolveGuestClasspath() throws URISyntaxException {
        return Path.of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    }
}
