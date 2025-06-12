package com.example;

import org.apache.arrow.vector.Float8Vector;

public interface DataAnalysisPyModule {

    double calculateMean(Float8Vector valueVector);
    double calculateMedian(Float8Vector valueVector);
}
