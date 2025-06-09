## SVG Charts with GraalPy and Micronaut

This demo illustrates how GraalPy can be used to embed [Pygal](https://github.com/Kozea/pygal), a dynamic SVG charting library written in Python, in a Micronaut application.
In particular, this demo shows four different approaches to interact with Pygal from Java.

## Preparation

Install GraalVM for JDK 24 and set the value of `JAVA_HOME` accordingly.
We recommend using [SDKMAN!](https://sdkman.io/). (For other download options, see [GraalVM Downloads](https://www.graalvm.org/downloads/).)

```bash
sdk install java 24.0.1-graal
```

## Run the Application

To start the demo, simply run:

```bash
./mvnw package mn:run
```

When the demo runs, open the following URLs in a browser:

| URL                           | Service                       |
|:------------------------------|:------------------------------|
| http://localhost:8080/java    | [`PyGalServicePureJava`](src/main/java/com/example/PyGalServicePureJava.java)        |
| http://localhost:8080/python  | [`PyGalServicePurePython`](src/main/java/com/example/PyGalServicePurePython.java)      |
| http://localhost:8080/mixed   | [`PyGalServiceMixed`](src/main/java/com/example/PyGalServiceMixed.java)           |
| http://localhost:8080/dynamic | [`PyGalServiceValueAPIDynamic`](src/main/java/com/example/PyGalServiceValueAPIDynamic.java) |


## Implementation Details

The `DemoController` uses four services that all render the same XY chart using different implementations:

- [`PyGalServicePureJava`](src/main/java/com/example/PyGalServicePureJava.java) interacts with Pygal and Python using Java interfaces and `Value.as(Class<T> targetType)`. This is the recommended approach.
- [`PyGalServicePurePython`](src/main/java/com/example/PyGalServicePurePython.java) embeds the Python sample code from the Pygal documentation.
- [`PyGalServiceMixed`](src/main/java/com/example/PyGalServiceMixed.java) uses a Python function which is invoked with Java values.
- [`PyGalServiceValueAPIDynamic`](src/main/java/com/example/PyGalServiceValueAPIDynamic.java) uses the [Value](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Value.html) API from the GraalVM SDK.


The `DemoTest` ensures that all four service implementations render the same XY chart. Run it with `./mvnw test`.

> Note: This demo uses a single [`GraalPyContext`](src/main/java/com/example/GraalPyContext.java), which can execute [Python code in only one thread at a time](https://docs.python.org/3/glossary.html#term-global-interpreter-lock).
> Threads running Python code are internally scheduled in round-robin fashion.
> Pure Python packages including Pygal can be used in multiple GraalPy contexts, for example one context per thread, to improve the throughput of the application.
> Other demos such as [`graalwasm-micronaut-photon`](../../graalwasm/graalwasm-micronaut-photon) illustrate how to pool multiple contexts.
