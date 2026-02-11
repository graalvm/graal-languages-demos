package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 6, time = 10)
@Measurement(iterations = 6, time = 30)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class OnnxBenchmark {

    private OnnxRunner runner;
    private byte[] modelData;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        runner = new OnnxRunner();
        modelData = Files.readAllBytes(Paths.get("src/main/resources/modeel.onnx"));
    }

    @Benchmark
    public void benchmarkPredict(Blackhole blackhole) {
        runner.predict(modelData,blackhole);
    }
}
