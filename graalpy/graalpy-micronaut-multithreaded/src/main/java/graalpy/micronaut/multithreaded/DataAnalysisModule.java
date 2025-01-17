package graalpy.micronaut.multithreaded;

public interface DataAnalysisModule {
    double calculateMean(String csv, int column);
    double calculateMedian(String csv, int column);
    String describe(String csv);
}
