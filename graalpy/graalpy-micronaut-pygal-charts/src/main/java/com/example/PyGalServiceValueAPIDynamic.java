/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;


import jakarta.inject.Singleton;
import org.graalvm.polyglot.Value;

import java.util.stream.IntStream;

@Singleton
public class PyGalServiceValueAPIDynamic implements PyGalService {
    private final Value pyGalModule;

    PyGalServiceValueAPIDynamic(GraalPyContext context) {
        pyGalModule = context.eval("import pygal; pygal");
    }

    @Override
    public String renderXYChart() {
        Value xyChart = pyGalModule.invokeMember("XY");
        xyChart.putMember("title", "XY Cosinus");
        xyChart.invokeMember("add", "x = cos(y)", IntStream.range(-50, 50).filter(x -> x % 5 == 0).mapToObj(x -> new double[]{Math.cos(x / 10.0), x / 10.0}).toArray(double[][]::new));
        xyChart.invokeMember("add", "y = cos(x)", IntStream.range(-50, 50).filter(x -> x % 5 == 0).mapToObj(x -> new double[]{x / 10.0, Math.cos(x / 10.0)}).toArray(double[][]::new));
        xyChart.invokeMember("add", "x = 1", new int[][]{{1, -5}, {1, 5}});
        xyChart.invokeMember("add", "x = -1", new int[][]{{-1, -5}, {-1, 5}});
        xyChart.invokeMember("add", "y = 1", new int[][]{{-5, 1}, {5, 1}});
        xyChart.invokeMember("add", "y = -1", new int[][]{{-5, -1}, {5, -1}});
        return xyChart.invokeMember("render").invokeMember("decode").asString();
    }
}
