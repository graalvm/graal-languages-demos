# GraalPy Quick Start

A minimal Java application that embeds Python code with GraalPy.

## Preparation

Install GraalVM 25 and set the value of `JAVA_HOME` accordingly.
We recommend using [SDKMAN!](https://sdkman.io/). (For other download options, see [GraalVM Downloads](https://www.graalvm.org/downloads/).)

```bash
sdk install java 25-graal
```

## Run the Application Using Maven

To build and test the demo, run:

```bash
./mvnw test
```

To execute the main method, run:

```bash
./mvnw exec:java
```

## Run the Application Using Gradle

To build and test the demo, run:

```bash
./gradlew test
```

To execute the main method, run:

```bash
./gradlew run
```
