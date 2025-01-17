package graalpy.micronaut.multithreaded;

import java.util.concurrent.ExecutionException;

import io.micronaut.context.annotation.Bean;

@Bean
public class DataAnalysisModuleMultiContext implements DataAnalysisModule {

    private final PythonPool pool;

    public DataAnalysisModuleMultiContext(PythonPool pool) {
        this.pool = pool;
        for (int i = 0; i < pool.getPoolSize(); i++) {
            pool.submit(() -> getModule());
        }
    }

    private DataAnalysisModule getModule() {
        return pool.eval("import data_analysis; data_analysis").as(DataAnalysisModule.class);
    }

    @Override
    public double calculateMean(String csv, int column) {
        try {
            return pool.submit(() -> getModule().calculateMean(csv, column)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double calculateMedian(String csv, int column) {
        try {
            return pool.submit(() -> getModule().calculateMedian(csv, column)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String describe(String csv) {
        try {
            return pool.submit(() -> getModule().describe(csv)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
