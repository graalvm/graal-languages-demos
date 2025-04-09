/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.graalvm.continuations.Continuation;

class MyJavaSerializer implements PersistentApp.MySerializer {
    @Override
    public Continuation load(Path storagePath) throws IOException, ClassNotFoundException {
        try (var in = new ObjectInputStream(Files.newInputStream(storagePath, READ))) {
            return (Continuation) in.readObject();
        }
    }

    @Override
    public void saveTo(Continuation continuation, Path storagePath) throws IOException {
        // Will overwrite previously existing file if any.
        try (var out = new ObjectOutputStream(Files.newOutputStream(storagePath, CREATE, TRUNCATE_EXISTING, WRITE))) {
            out.writeObject(continuation);
        }
    }
}
