/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@SpringBootTest
class GraalJSSpringBootD3ApplicationTests {

	@Autowired
	private GraphService graphService;

	@Test
	void testGenerateGraph() {

		Model model = new ConcurrentModel();
		String result = graphService.generateGraph(model);

		Assertions.assertEquals("graph", result);
		String svgContent = (String) model.getAttribute("svgContent");
		Assertions.assertNotNull(svgContent, "SVG content must not be null");
		Assertions.assertTrue(svgContent.contains("<svg") && svgContent.contains("</svg>"),
				"SVG content must be valid (containing <svg> and </svg> tags)");
	}

}
