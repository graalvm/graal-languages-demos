package com.example;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.Float8Vector;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.utils.GraalPyResources;

import java.io.IOException;

public class Main {

    private static DataAnalysisPyModule dataAnalysisPyModule;
    private static final String PYTHON_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v241.csv";
    private static final String JAVASCRIPT_URL = "https://www.graalvm.org/compatibility/module_results/js-module-testing.csv";
    private static final Integer PASSING_RATE_COLUMN_INDEX = 3;

    public static Context initContext() {
        return GraalPyResources.contextBuilder().allowAllAccess(true).build();
    }

    public static void initDataAnalysisPyModule(Context context) {
        Value value = context.eval("python", "import data_analysis; data_analysis");
        dataAnalysisPyModule = value.as(DataAnalysisPyModule.class);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        try (Context context = initContext();
             BufferAllocator allocator = new RootAllocator();
             Float8Vector pyVector = new Float8Vector("pyPassingRate", allocator);
             Float8Vector jsVector = new Float8Vector("jsPassingRate", allocator)
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