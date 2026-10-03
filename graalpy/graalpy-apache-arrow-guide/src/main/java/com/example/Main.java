package com.example;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.python.embedding.VirtualFileSystem;

public class Main {

    private static DataAnalysisPyModule dataAnalysisPyModule;

    public static Context initContext() throws IOException {
        var resourcesDir = Path.of(System.getProperty("user.home"), ".cache", "graalpy-apache-arrow-guide.resources"); // ①
        var fs = VirtualFileSystem.create();
        GraalPyResources.extractVirtualFileSystemResources(fs, resourcesDir); // ②
        return GraalPyResources.contextBuilder(resourcesDir)
                .option("python.WarnExperimentalFeatures", "false")
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(_ -> true)
                .allowNativeAccess(true)
                .build(); // ③
    }

    public static void initDataAnalysisPyModule(Context context) {
        Value value = context.eval("python", "import data_analysis; data_analysis");
        dataAnalysisPyModule = value.as(DataAnalysisPyModule.class);
    }

    private static final String PYTHON_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v241.csv";
    private static final String JAVASCRIPT_URL = "https://www.graalvm.org/compatibility/module_results/js-module-testing.csv";
    private static final Integer PASSING_RATE_COLUMN_INDEX = 3;


    public static void main(String[] args) throws IOException, InterruptedException {
        try (Context context = initContext();
             BufferAllocator allocator = new RootAllocator();
             Float8Vector pyVector = new Float8Vector("python", allocator);
             Float8Vector jsVector = new Float8Vector("javascript", allocator)
        ) {
            initDataAnalysisPyModule(context);
            Thread pyThread = new Thread(() -> DownloadUtils.downloadAndStore(PYTHON_URL, PASSING_RATE_COLUMN_INDEX, pyVector));
            Thread jsThread = new Thread(() -> DownloadUtils.downloadAndStore(JAVASCRIPT_URL, PASSING_RATE_COLUMN_INDEX, jsVector));
            pyThread.start();
            jsThread.start();
            pyThread.join();
            jsThread.join();

            System.out.println("Python mean: " + dataAnalysisPyModule.calculateMean(pyVector));
            System.out.println("Python median: " + dataAnalysisPyModule.calculateMedian(pyVector));
            System.out.println("JS mean: " + dataAnalysisPyModule.calculateMean(jsVector));
            System.out.println("JS median: " + dataAnalysisPyModule.calculateMedian(jsVector));
        }
    }
}
