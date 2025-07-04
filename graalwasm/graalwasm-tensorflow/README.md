# Tensorflow with GraalWasm Spring-boot Demo

This is a simple Spring Boot application that predicts house prices using a JavaScript function executed via GraalVM, with a web frontend built using Thymeleaf.
This demo illustrates how GraalWasm can be used to embed tensorflow-wasm . The demo also uses GraalJS to access the Tensorflow module through the WebAssembly JavaScript API.

## Run the Application

To start the demo, simply run:

```bash
mvn spring-boot:run
```

When the demo runs, open the following URLs in a browser:
Go to: http://localhost:8080