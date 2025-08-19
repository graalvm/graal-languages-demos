/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example.demo;

import com.example.demo.GraalPyContextConfiguration.GraalPyContext;
import org.graalvm.polyglot.Value;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;


@Service
@ImportRuntimeHints(PyGalServicePureJava.PyGalServicePureJavaRuntimeHints.class)
public class PyGalServicePureJava implements PyGalService {
    private final PyGal pyGalModule;

    PyGalServicePureJava(GraalPyContext graalPyContext) {
        pyGalModule = graalPyContext.eval("import pygal; pygal").as(PyGal.class);
    }

    public interface PyGal {
        XY XY();
    }

    public interface XY {
        default void title(String title) {
            Value.asValue(this).putMember("title", title);
        }

        void add(String label, Object[] values);

        BytesIO render();
    }

    public interface BytesIO {
        String decode();
    }

    @Override
    public String renderXYChart() {
        XY xyChart = pyGalModule.XY();
        xyChart.title("XY Cosinus");
        xyChart.add("x = cos(y)", IntStream.range(-50, 50).filter(x -> x % 5 == 0).mapToObj(x -> new double[]{Math.cos(x / 10.0), x / 10.0}).toArray(double[][]::new));
        xyChart.add("y = cos(x)", IntStream.range(-50, 50).filter(x -> x % 5 == 0).mapToObj(x -> new double[]{x / 10.0, Math.cos(x / 10.0)}).toArray(double[][]::new));
        xyChart.add("x = 1", new int[][]{{1, -5}, {1, 5}});
        xyChart.add("x = -1", new int[][]{{-1, -5}, {-1, 5}});
        xyChart.add("y = 1", new int[][]{{-5, 1}, {5, 1}});
        xyChart.add("y = -1", new int[][]{{-5, -1}, {5, -1}});
        return xyChart.render().decode();
    }

    static class PyGalServicePureJavaRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register interfaces as proxy for Value.as().
            hints.proxies().registerJdkProxy(PyGal.class);
            hints.proxies().registerJdkProxy(XY.class);
            hints.proxies().registerJdkProxy(BytesIO.class);

            // Provide access to title default method.
            hints.reflection().registerType(XY.class, MemberCategory.INTROSPECT_DECLARED_METHODS);
        }
    }
}
