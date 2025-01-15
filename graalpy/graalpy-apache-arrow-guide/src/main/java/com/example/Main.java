package com.example;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.utils.GraalPyResources;
import org.graalvm.python.embedding.utils.VirtualFileSystem;

public class Main {

    private static DataAnalysisPyModule dataAnalysisPyModule;
    private static final String PYTHON_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v241.csv";
    private static final String JAVASCRIPT_URL = "https://www.graalvm.org/compatibility/module_results/js-module-testing.csv";
    private static final Integer PASSING_RATE_COLUMN_INDEX = 3;

    public static Context initContext() throws IOException {
        var resourcesDir = Path.of(System.getProperty("user.home"), ".cache", "graalpy-apache-arrow-guide.resources");
        if (!resourcesDir.toFile().isDirectory()) {
            var fs = VirtualFileSystem.create();
            GraalPyResources.extractVirtualFileSystemResources(fs, resourcesDir);
        }
        return GraalPyResources.contextBuilder(resourcesDir)
            .option("python.PythonHome", "")
            .option("python.WarnExperimentalFeatures", "false")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(_ -> true)
            .allowNativeAccess(true)
            .build();
    }

    public static void initDataAnalysisPyModule(Context context) {
        Value value = context.eval("python", "import data_analysis; data_analysis");
        dataAnalysisPyModule = value.as(DataAnalysisPyModule.class);
    }


    public static void main(String[] args) throws IOException {
        try (Context context = initContext();
             BufferAllocator allocator = new RootAllocator();
        ) {
            initDataAnalysisPyModule(context);
            try (ExecutorService e =  Executors.newWorkStealingPool()) {
                // Simulate some amount of parallelism
                for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; ++i) {
                    for (var pair : Map.of("GraalPy", PYTHON_URL, "GraalJS", JAVASCRIPT_URL).entrySet()) {
                        e.submit(() -> {
                            try (Float8Vector v = new Float8Vector("passingRate", allocator)) {
                                DownloadUtils.downloadAndStore(pair.getValue(), PASSING_RATE_COLUMN_INDEX, v);
                                System.err.println("DOWNLOAD FINISHED, SUBMITTING TO PYTHON WITH 0-COPY IN 5 SECONDS! WATCH THE MEMORY AND BE AMAZED!");
                                try {
                                    Thread.sleep(Duration.ofSeconds(5));
                                } catch (InterruptedException e1) {
                                }
                                System.out.println(pair.getKey() + " mean: " + dataAnalysisPyModule.calculateMean(v));
                                System.out.println(pair.getKey() + " median: " + dataAnalysisPyModule.calculateMedian(v));
                            }
                        });
                    }
                }
            }
        }
    }
}
