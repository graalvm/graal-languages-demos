/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package org.example;

import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;
import java.util.Map;

@Controller // ①
public class SentimentAnalysisController {

    private final VaderSentiment.SentimentIntensityAnalyzer sentimentAnalysis;

    SentimentAnalysisController(VaderSentiment vaderSentiment) { // ②
        this.sentimentAnalysis = vaderSentiment.SentimentIntensityAnalyzer();
    }

    @Get // ③
    @View("index") // ④
    public void index() {

    }

    @Get(value = "/analyze") // ⑤
    @ExecuteOn(TaskExecutors.BLOCKING) // ⑥
    public Map<String, Double> answer(String text) {
        return sentimentAnalysis.polarity_scores(text); // ⑦
    }
}
