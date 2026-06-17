# Excelize with GraalWasm Micronaut Demo

This demo illustrates how GraalWasm can be used to embed [excelize-wasm](https://github.com/xuri/excelize-wasm/), a WebAssembly build of the Go [Excelize](https://github.com/xuri/excelize) library for reading and writing Microsoft Excel™ spreadsheets.
The demo also uses GraalJS to access the Excelize module through the WebAssembly JavaScript API.

## Preparation

Install GraalVM for JDK 24 and set the value of `JAVA_HOME` accordingly.
We recommend using [SDKMAN!](https://sdkman.io/). (For other download options, see [GraalVM Downloads](https://www.graalvm.org/downloads/).)
```bash
sdk install java 24-graal
```

## Run the Application

To start the demo, simply run:

```bash
./mvnw package mn:run
```

When the demo runs, open the following URLs in a browser:

- http://localhost:8080/
  

This interface offers two core features:

Import Excel to Database – Upload an Excel (.xlsx) file to parse and store its contents in the backend database.
Export Database to Excel – Retrieve data from the database and download it as a structured Excel (.xlsx) file.
Ideal for seamless data management via the browser.




## Implementation Details

The [`Controller`](src/main/java/com/example/Controller.java) uses a [`Service`](src/main/java/com/example/ExcelizeService.java) That integrates the Excelize library with Micronaut and GraalVM to generate and read Excel files using WebAssembly (WASM) and JavaScript. It loads necessary JS and WASM files, executes JavaScript to manipulate Excel data, and reads the content back into a Java application. The results are saved as Excel files or processed into Java objects.

