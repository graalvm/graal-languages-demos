package org.example;

import org.graalvm.polyglot.Value;

import java.util.function.Consumer;

@FunctionalInterface
public interface ValueConsumer extends Consumer<Value> {
    @Override
    void accept(Value value);
}