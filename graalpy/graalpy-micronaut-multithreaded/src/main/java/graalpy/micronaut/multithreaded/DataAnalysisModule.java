package graalpy.micronaut.multithreaded;

import io.micronaut.graal.graalpy.annotations.GraalPyModule;

@GraalPyModule("data_analysis")
public interface DataAnalysisModule {
    double calculateMean(String csv, int column);
    double calculateMedian(String csv, int column);
    String describe(String csv);
}
