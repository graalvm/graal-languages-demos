/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;

@Controller
public class GraphController {

    private final GraphService graphService;

    GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/graph")
    public String displayGraph(Model model) {
        return graphService.generateGraph(model);
    }

}