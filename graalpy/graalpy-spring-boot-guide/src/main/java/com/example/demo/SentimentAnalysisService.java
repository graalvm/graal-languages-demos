/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example.demo;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ImportRuntimeHints(SentimentAnalysisService.SentimentIntensityAnalyzerRuntimeHints.class)
public class SentimentAnalysisService {
    private final SentimentIntensityAnalyzer sentimentIntensityAnalyzer;

    public SentimentAnalysisService(GraalPyContext context) {
        var value = context.eval("""
                from vader_sentiment.vader_sentiment import SentimentIntensityAnalyzer
                SentimentIntensityAnalyzer() # ①
                """);
        sentimentIntensityAnalyzer = value.as(SentimentIntensityAnalyzer.class); // ②
    }

    public Map<String, Double> getSentimentScore(String text) {
        return sentimentIntensityAnalyzer.polarity_scores(text); // ③
    }

    static class SentimentIntensityAnalyzerRuntimeHints implements RuntimeHintsRegistrar { // ④
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.proxies().registerJdkProxy(SentimentIntensityAnalyzer.class);
        }
    }
}
