# Photon with GraalWasm and Spring Boot Demo

This demo illustrates how GraalWasm can be used to embed [Photon](https://silvia-odwyer.github.io/photon/), a WebAssembly image processing library written in Rust, in a Spring Boot application.
The demo also uses GraalJS to access the Photon module through the WebAssembly JavaScript API.

## Preparation

Install GraalVM 25 and set the value of `JAVA_HOME` accordingly.
We recommend using [SDKMAN!](https://sdkman.io/). (For other download options, see [GraalVM Downloads](https://www.graalvm.org/downloads/).)

```bash
sdk install java 25.0.1-graal
```

## Run the Application

To start the demo, simply run:

```bash
./mvnw spring-boot:run
```

When the demo runs, open http://localhost:8080/ in a browser.
To apply a specific effect, navigate to `http://localhost:8080/photo/<effect name>` (e.g., http://localhost:8080/photo/colorize).

### Compiling with GraalVM Native Image

To compile the application with GraalVM Native Image, run:

```bash
./mvnw -Pnative native:compile
./target/demo
```

To use [Profile-Guided Optimization](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/PGO/), run the following commands:

```bash
# Compile and run instrumented image
./mvnw -Pnative,pgo-instrument native:compile
./target/demo-g1-pgo-instrument

# Produce some load, for example using https://github.com/rakyll/hey
hey -c 8 -z 2m http://localhost:8080/photo/flipv
# Quitting the demo-g1-pgo-instrument process will generate a profile file (default.iprof)

# Compile and run optimized image
./mvnw -Pnative,pgo native:compile
./target/demo-g1-pgo
```

### Enable Wasm Debugging

GraalWasm has sophisticated support for Wasm debugging based on DWARF debug info.
When you enable debugging, you can step through and debug the Rust sources of Photon and its JavaScript binding.
Enable the `wasm-debug` profile to recompile Photon with DWARF debug info and set the `inspect` or `dap` system property to enable the [Chrome DevTools Protocol](https://www.graalvm.org/latest/tools/chrome-debugger/) or the [Debug Adapter Protocol (DAP)](https://www.graalvm.org/latest/tools/dap/) respectively.

```bash
# Enable DWARF debug info and the Chrome DevTools Protocol
 ./mvnw spring-boot:run -Pwasm-debug -Dinspect=true
 
 # Enable DWARF debug info and the Debug Adapter Protocol
 ./mvnw spring-boot:run -Pwasm-debug -Ddap=true
```

When enabling `inspect`, you should see debug sessions details after the Spring banner and some log output, and before the application has fully started.
Click on the link starting with `ws://` in IntelliJ Ultimate to start a debugging session, or open the link starting with `devtools://` in Chrome.

```bash
   .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
...
Debugger listening on ws://127.0.0.1:9229/PeRmlqJY_GRtANWC9LCuKyKc8OFkuCHXwcx0jVkR8Pw
For help, see: https://www.graalvm.org/tools/chrome-debugger
E.g. in Chrome open: devtools://devtools/bundled/js_app.html?ws=127.0.0.1:9229/PeRmlqJY_GRtANWC9LCuKyKc8OFkuCHXwcx0jVkR8Pw
```

When enabling `dap`, you should see that "Graal DAP" is enabled and on which port it is listening:

```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
...
[Graal DAP] Starting server and listening on localhost/127.0.0.1:4711
```

In VS Code, use the following launch configuration (`launch.json`) to attach to the debugging server:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Attach",
      "type": "node",
      "request": "attach",
      "debugServer": 4711
    }
  ]
}
```

You can also enable the `wasm-debug` profile when compiling the application with GraalVM Native Image:

```bash
# Compile image with DWARF debug info
./mvnw -Pnative,wasm-debug native:compile

# Run the app and enable the Chrome DevTools Protocol
./target/demo-wasm-debug -Dinspect=true

# Run the app and enable the Debug Adapter Protocol
./target/demo-wasm-debug -Ddap=true
```

Note that the debug build of Photon has additional optimizations disabled in the Rust toolchain, causing the application to perform slower compared with the result build.
Also note, that the DWARF debug info increases the file size of the Photon Wasm module from ~1.5M to ~26M, and with that also the file size of the JARs and native images.

## Implementation Details

The [`DemoController`](src/main/java/com/example/demo/DemoController.java) uses a [`PhotonService`](src/main/java/com/example/demo/PhotonService.java) to implement the `/photo/{effectName}` endpoint.
This service accesses `Photon` objects that are pooled in a [`PhotonPool`](src/main/java/com/example/demo/PhotonPool.java) to check whether an effect for a given effectName exists before applying the effect to a sample image.

`PhotonPool` creates a `Photon` object for each available processor, which in turn holds a reference to the corresponding `photonModule` and an `imageContent` object.
Both of these objects are from JavaScript and backed by the same [`Context`](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Context.html).
Note that the `Context` objects share the same [`Engine`](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Engine.html), to improve warmup and memory footprint.

Also note that the Photon JavaScript and WebAssembly modules as well as the sample images are downloaded when the demo is built with the `wagon-maven-plugin` Maven plugin (see _pom.xml_).

The [`DemoApplicationTests`](src/test/java/com/example/demo/DemoApplicationTests.java) tests that applying the same effect on two copies of the same image yields the same result, regardless of the effect used.
Run it with `./mvnw test`.
