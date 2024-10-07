/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import jakarta.inject.Singleton;

@Singleton
public class PyGalServicePurePython implements PyGalService {
    private final String purePythonXY;

    PyGalServicePurePython(GraalPyContext graalPyContext) {
        purePythonXY = graalPyContext.eval(
                // language=python
                """
                        import pygal
                        from math import cos
                        xy_chart = pygal.XY()
                        xy_chart.title = 'XY Cosinus'
                        xy_chart.add('x = cos(y)', [(cos(x / 10.), x / 10.) for x in range(-50, 50, 5)])
                        xy_chart.add('y = cos(x)', [(x / 10., cos(x / 10.)) for x in range(-50, 50, 5)])
                        xy_chart.add('x = 1',  [(1, -5), (1, 5)])
                        xy_chart.add('x = -1', [(-1, -5), (-1, 5)])
                        xy_chart.add('y = 1',  [(-5, 1), (5, 1)])
                        xy_chart.add('y = -1', [(-5, -1), (5, -1)])
                        xy_chart.render().decode()
                        """).asString();
    }

    @Override
    public String renderXYChart() {
        return purePythonXY;
    }
}
