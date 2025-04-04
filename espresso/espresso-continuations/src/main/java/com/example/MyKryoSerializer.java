/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.graalvm.continuations.Continuation;
import org.graalvm.continuations.ContinuationSerializable;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.KryoObjectInput;
import com.esotericsoftware.kryo.io.KryoObjectOutput;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;

class MyKryoSerializer implements PersistentApp.MySerializer {
    // We set up the Kryo engine here.
    private static final Kryo kryo = setupKryo();

    private static Kryo setupKryo() {
        var kryo = new Kryo();
        // The heap will have cycles, and Kryo requires us to opt in to support for that.
        kryo.setReferences(true);
        // We do not want to manually register everything, as heap contents are dynamic.
        kryo.setRegistrationRequired(false);
        // Be able to create objects even if they lack a no-arg constructor.
        kryo.setInstantiatorStrategy(
                new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        /*
         * Register a custom serializer for continuation objects.
         * All serialization-relevant classes in the Continuation API will extend the `ContinuationSerializable` class.
         */
        kryo.addDefaultSerializer(ContinuationSerializable.class, new ContinuationSerializer());
        return kryo;
    }

    /**
     * A custom Kryo `Serializer` for continuation objects.
     */
    static class ContinuationSerializer extends Serializer<ContinuationSerializable> {
        public ContinuationSerializer() {
            super(false, false);
        }

        @Override
        public void write(Kryo kryo, Output output, ContinuationSerializable object) {
            try {
                ContinuationSerializable.writeObjectExternal(object, new KryoObjectOutput(kryo, output));
            } catch (IOException e) {
                throw new KryoException(e);
            }
        }

        @Override
        public ContinuationSerializable read(Kryo kryo, Input input, Class<? extends ContinuationSerializable> type) {
            try {
                /*
                 * The continuation deserialization mechanism will use this classloader to load the classes present on the heap.
                 * Kryo requires awareness of created objects in order to handle cycles in the serialized object graph.
                 * Let Kryo know about the deserialized objects using kryo::reference.
                 */
                return ContinuationSerializable.readObjectExternal(type, new KryoObjectInput(kryo, input),
                        kryo.getClassLoader(),
                        kryo::reference);
            } catch (IOException | ClassNotFoundException e) {
                throw new KryoException(e);
            }
        }
    }

    @Override
    public Continuation load(Path storagePath) throws IOException {
        try (var in = new Input(Files.newInputStream(storagePath, READ))) {
            return kryo.readObject(in, Continuation.class);
        }
    }

    @Override
    public void saveTo(Continuation continuation, Path storagePath) throws IOException {
        try (var out = new Output(Files.newOutputStream(storagePath, CREATE, TRUNCATE_EXISTING, WRITE))) {
            kryo.writeObject(out, continuation);
        }
    }
}
