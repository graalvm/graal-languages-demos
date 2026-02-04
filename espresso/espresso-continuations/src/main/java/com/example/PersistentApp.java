/*
 * Copyright (c) 2024, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import java.io.IOException;


import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.graalvm.continuations.Continuation;
import org.graalvm.continuations.ContinuationEntryPoint;
import org.graalvm.continuations.SuspendCapability;

/**
 * Application that persists its state by saving it to a file.
 * <p>
 * Each time the program is run, it increments the counter and prints out the new value before quitting.
 * <p>
 * By default, the state is persisted to a file named "state.serial.bin" in the current working directory,
 * but it can be changed by specifying a new path with the {@code "-p <path>"} option.
 * <p>
 * By default, standard Java serialization is used, but "Kryo" can be selected with the {@code "-s kryo"} option.
 * <p>
 * The continuation payload must implement `ContinuationEntryPoint`.
 * This class is also `Serializable` to work with Java serialization.
 */
public class PersistentApp implements ContinuationEntryPoint, Serializable {
    /**
     * An interface for serializing/deserializing a continuation to the file system.
     * Two implementations are showcased later: one for `Java` and one for `Kryo`.
     */
    public interface MySerializer {
        Continuation load(Path storagePath) throws IOException, ClassNotFoundException;

        void saveTo(Continuation continuation, Path storagePath) throws IOException;
    }

    private static final String DEFAULT_PATH = "state.serial.bin";

    int counter = 0;

    /**
     * Anything reachable from the stack in this method is persisted, including 'this'.
     * <p>
     * Suspending a continuation requires access to this “suspend capability” object.
     * By controlling who gets access to it, you can work out where a suspension might occur.
     * If you do not want this, the capability can be stored it in a static `ThreadLocal` and let anything suspend.
     */
    @Override
    public void start(SuspendCapability suspendCapability) {
        while (true) {
            counter++;
            System.out.println("The counter value is now " + counter);

            doWork(suspendCapability);
        }
    }

    private static void doWork(SuspendCapability suspendCapability) {
        // Do something ...
        /*
         * The call to `suspend` causes control flow to return from the call to resume below.
         * The state of the application will be written to the file system and it will carry on when the user starts the application again.
         */
        suspendCapability.suspend();
        // Do something else ...
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        checkSupported();

        Path storagePath = getStoragePath(args);
        MySerializer ser = getSerializer(args);

        Continuation continuation = loadOrInit(storagePath, ser);
        /*
         * Control flow will either begin at `start` for the first program execution,
         * or jump to after the call to `suspend` above for later executions.
         */
        continuation.resume();
        ser.saveTo(continuation, storagePath);
    }

    private static void checkSupported() {
        try {
            if (!Continuation.isSupported()) {
                System.err.println("Ensure you are running on an Espresso VM with the flags '--experimental-options --java.Continuum=true'.");
                System.exit(1);
            }
        } catch (NoClassDefFoundError e) {
            System.err.println("Please make sure you are using a VM that supports the Continuation API");
            System.exit(1);
        }
    }

    /////////////////////////////////////////////////////////////
    // Code to load, save and resume the state of the program. //
    /////////////////////////////////////////////////////////////

    private static Path getStoragePath(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-p") && (args.length > i + 1)) {
                return Paths.get(args[i + 1]);
            }
        }

        return Paths.get(DEFAULT_PATH);
    }

    private static Continuation loadOrInit(Path storagePath, MySerializer ser) throws IOException, ClassNotFoundException {
        Continuation continuation;
        if (!Files.exists(storagePath)) {
            /*
             * First execution of the program with the specified path: use a fresh continuation.
             */
            continuation = Continuation.create(new PersistentApp());
        } else {
            /*
             * Program had been executed at least once with the specified path: restore the continuation from file.
             */
            continuation = ser.load(storagePath);
        }
        return continuation;
    }

    private static MySerializer getSerializer(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-s") && (args.length > i + 1)) {
                String key = args[i + 1];
                if (key.equals("java")) {
                    return new MyJavaSerializer();
                }
                if (key.equals("kryo")) {
                    return new MyKryoSerializer();
                }
            }
        }
        return new MyJavaSerializer();
    }
}
