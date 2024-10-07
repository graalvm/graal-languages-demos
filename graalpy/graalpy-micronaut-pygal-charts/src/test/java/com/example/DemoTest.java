/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

@MicronautTest
class DemoTest {

    @Inject
    private PyGalServicePureJava pyGalServicePureJava;
    @Inject
    private PyGalServicePurePython pyGalServicePurePython;
    @Inject
    private PyGalServiceMixed pyGalServiceMixed;
    @Inject
    private PyGalServiceValueAPIDynamic pyGalServiceValueAPIDynamic;

    @Test
    void testIdentity() {
        // Patterns to remove unique identifiers that PyGal adds every time a chart is rendered
        String identifierPattern = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}";
        String chartPattern = "chart-" + identifierPattern;
        String pyGalConfigPattern = "pygal\\.config\\['" + identifierPattern + "']";

        // We use a HashSet to check whether the charts are identical
        HashSet<String> xyCharts = new HashSet<>();
        for (PyGalService service : List.of(pyGalServicePureJava, pyGalServicePurePython, pyGalServiceMixed, pyGalServiceValueAPIDynamic)) {
            String xyChart = service.renderXYChart();
            String xzChartSanitized = xyChart.replaceAll(chartPattern, "chart").replaceAll(pyGalConfigPattern, "pygal.config");
            xyCharts.add(xzChartSanitized);
        }
        Assertions.assertEquals(1, xyCharts.size(), "xyCharts are not identical");
    }
}
