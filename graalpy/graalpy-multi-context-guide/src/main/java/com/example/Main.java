package com.example;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final String PYTHON241_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v241.csv";
    private static final String PYTHON240_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v240.csv";
    private static final String PYTHON231_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v231.csv";
    private static final Integer PASSING_RATE_COLUMN_INDEX = 3;

    public static void main(String[] args) {
        try (PythonPool p = PythonPool.newFixedPool(4)) {
            List.of(
                    p.submit(() -> System.out.println("GraalPy 24.1 mean: " + getDataAnalysisModule(p).calculateMean(DownloadUtils.download(PYTHON241_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("GraalPy 24.0 mean: " + getDataAnalysisModule(p).calculateMean(DownloadUtils.download(PYTHON240_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("GraalPy 23.1 mean: " + getDataAnalysisModule(p).calculateMean(DownloadUtils.download(PYTHON231_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("JS mean: " + getDataAnalysisModule(p).calculateMean(DownloadUtils.download(PYTHON231_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("GraalPy 24.1 median: " + getDataAnalysisModule(p).calculateMedian(DownloadUtils.download(PYTHON241_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("GraalPy 24.0 median: " + getDataAnalysisModule(p).calculateMedian(DownloadUtils.download(PYTHON240_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("GraalPy 23.1 median: " + getDataAnalysisModule(p).calculateMedian(DownloadUtils.download(PYTHON231_URL), PASSING_RATE_COLUMN_INDEX))),
                    p.submit(() -> System.out.println("JS median: " + getDataAnalysisModule(p).calculateMedian(DownloadUtils.download(PYTHON231_URL), PASSING_RATE_COLUMN_INDEX)))
            ).parallelStream().forEach(f -> {
                try {
                    System.err.println(f.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static DataAnalysisPyModule getDataAnalysisModule(PythonPool p) {
        return p.eval("import data_analysis; data_analysis").as(DataAnalysisPyModule.class);
    }
}
