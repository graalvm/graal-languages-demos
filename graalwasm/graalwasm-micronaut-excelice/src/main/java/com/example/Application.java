package com.example;

import io.micronaut.runtime.Micronaut;

import java.io.IOException;


public class Application {
    public static void main(String[] args) throws IOException {


        Micronaut.run(Application.class, args);
    }
}


