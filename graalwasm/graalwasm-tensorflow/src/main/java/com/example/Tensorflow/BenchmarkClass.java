package com.example.Tensorflow;

import org.graalvm.polyglot.Context;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 6, time = 10)
@Measurement(iterations = 6, time = 10)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class BenchmarkClass {
    ContextPool contextPool;

    public BenchmarkClass()  {
        try {
            this.contextPool = new ContextPool();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize ContextPool", e);
        }
    }

    @Benchmark
    public void predict(Blackhole blackhole){
        Context context = contextPool.getContext();
        try {
            PredictFunction predictFn = context.getBindings("js").getMember("predictHouse").as(PredictFunction.class);

            double[] houseFeatures = new double[12];
            for (int i = 0; i < houseFeatures.length; i++) {
                houseFeatures[i] = ThreadLocalRandom.current().nextDouble(1, 5000);
            }

            Promise<List<List<Double>>> prediction = predictFn.apply(houseFeatures);

            prediction.then(result -> blackhole.consume(result.get(0).get(0)));
        } finally {
            contextPool.release(context);
        }
    }

    public interface PredictFunction {
        Promise<List<List<Double>>> apply(double[] houseFeatures);
    }

    public interface Promise<T> {
        void then(Callback<T> callback);
    }

    @FunctionalInterface
    public interface Callback<T> {
        void accept(T result);
    }
}