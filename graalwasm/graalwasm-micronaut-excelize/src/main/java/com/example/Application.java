/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package com.example;

import io.micronaut.runtime.Micronaut;
import java.io.IOException;


public class Application {
    public static void main(String[] args) throws IOException {
        Micronaut.run(Application.class, args);
    }
}


