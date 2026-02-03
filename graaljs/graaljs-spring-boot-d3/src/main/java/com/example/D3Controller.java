/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class D3Controller {

    private final D3Service d3Service;

    D3Controller(D3Service d3Service) {
        this.d3Service = d3Service;
    }

    @GetMapping("/d3-chord")
    public String renderChord(Model model) {
        return d3Service.renderChord(model);
    }

}