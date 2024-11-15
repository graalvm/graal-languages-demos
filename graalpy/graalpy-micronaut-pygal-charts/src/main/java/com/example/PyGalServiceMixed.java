/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.IntStream;

@Singleton
public class PyGalServiceMixed implements PyGalService {
    private final RenderXYFunction renderXYFunction;

    PyGalServiceMixed(GraalPyContext graalPyContext) {
        renderXYFunction = graalPyContext.eval(
                // language=python
                """
                        import pygal
                        
                        def render_xy(title, label_datapoint_entries):
                            xy_chart = pygal.XY()
                            xy_chart.title = title
                            for entry in label_datapoint_entries:
                                xy_chart.add(entry.label(), entry.dataPoints())
                            return xy_chart.render().decode()
                        
                        render_xy""").as(RenderXYFunction.class);
    }

    public record Entry(String label, double[][] dataPoints) {
    }

    @FunctionalInterface
    public interface RenderXYFunction {
        String apply(String title, List<Entry> labelDatapointEntries);
    }

    @Override
    public String renderXYChart() {
        String title = "XY Cosinus";
        List<Entry> labelDatapointEntries = List.of(
                new Entry("x = cos(y)", IntStream.range(-50, 50).filter(x -> x % 5 == 0).mapToObj(x -> new double[]{Math.cos(x / 10.0), x / 10.0}).toArray(double[][]::new)),
                new Entry("y = cos(x)", IntStream.range(-50, 50).filter(x -> x % 5 == 0).mapToObj(x -> new double[]{x / 10.0, Math.cos(x / 10.0)}).toArray(double[][]::new)),
                new Entry("x = 1", new double[][]{{1, -5}, {1, 5}}),
                new Entry("x = -1", new double[][]{{-1, -5}, {-1, 5}}),
                new Entry("y = 1", new double[][]{{-5, 1}, {5, 1}}),
                new Entry("y = -1", new double[][]{{-5, -1}, {5, -1}})
        );
        return renderXYFunction.apply(title, labelDatapointEntries);
    }
}
