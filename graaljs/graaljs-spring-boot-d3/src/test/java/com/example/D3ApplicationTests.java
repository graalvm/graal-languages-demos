/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@SpringBootTest
class D3ApplicationTests {

    @Autowired
    private D3Service d3Service;

    @Test
    void testRenderChord() {
        Model model = new ConcurrentModel();
        String result = d3Service.renderChord(model);

        Assertions.assertEquals("d3-chord", result);
        String svgContent = (String) model.getAttribute("svgContent");
        Assertions.assertNotNull(svgContent, "SVG content must not be null");
        Assertions.assertTrue(svgContent.contains("<svg") && svgContent.contains("</svg>"),
                "SVG content must be valid (containing <svg> and </svg> tags)");
    }

}
