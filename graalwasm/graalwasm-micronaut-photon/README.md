# Photon with GraalWasm Micronaut Demo

This demo illustrates how GraalWasm can be used to embed [Photon](https://silvia-odwyer.github.io/photon/), a WebAssembly image processing library written in Rust, in a Micronaut application.
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
./mvnw mn:run
```

When the demo runs, open http://localhost:8080/ in a browser.
To apply a specific effect, navigate to `http://localhost:8080/photo/<effect name>` (e.g., http://localhost:8080/photo/colorize).

### Compiling with GraalVM Native Image

To compile the application with GraalVM Native Image, run:

```bash
./mvnw package -Dpackaging=native-image
./target/demo
```

To use [Profile-Guided Optimization](https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/PGO/), run the following commands:

```bash
# Compile and run instrumented image
./mvnw package -Dpackaging=native-image -Ppgo-instrument
./target/demo-g1-pgo-instrument

# Produce some load, for example using https://github.com/rakyll/hey
hey -c 8 -z 2m http://localhost:8080/photo/flipv
# Quitting the demo-g1-pgo-instrument process will generate a profile file (default.iprof)

# Compile and run optimized image
./mvnw package -Dpackaging=native-image -Ppgo
./target/demo-g1-pgo
```

### Enable Wasm Debugging

GraalWasm has sophisticated support for Wasm debugging based on DWARF debug info.
When you enable debugging, you can step through and debug the Rust sources of Photon and its JavaScript binding.
Enable the `wasm-debug` profile to recompile Photon with DWARF debug info and set the `inspect` or `dap` system property to enable the [Chrome DevTools Protocol](https://www.graalvm.org/latest/tools/chrome-debugger/) or the [Debug Adapter Protocol (DAP)](https://www.graalvm.org/latest/tools/dap/), respectively.

```bash
# Enable DWARF debug info and the Chrome DevTools Protocol
 ./mvnw mn:run -Pwasm-debug -Dinspect=true
 
 # Enable DWARF debug info and the Debug Adapter Protocol
 ./mvnw mn:run -Pwasm-debug -Ddap=true
```

When enabling `inspect`, you should see debugging session details after the Micronaut banner and before the application has fully started.
Click on the link starting with `ws://` in IntelliJ Ultimate to start a debugging session, or open the link starting with `devtools://` in Chrome.

```bash
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
Debugger listening on ws://127.0.0.1:9229/vSOlrj5WsknK3-vm0AU4c8QqaxZpMn9rQSwuS16nb_k
For help, see: https://www.graalvm.org/tools/chrome-debugger
E.g. in Chrome open: devtools://devtools/bundled/js_app.html?ws=127.0.0.1:9229/vSOlrj5WsknK3-vm0AU4c8QqaxZpMn9rQSwuS16nb_k
```

When enabling `dap`, you should see that "Graal DAP" is enabled and on which port it is listening:

```bash
 __  __ _                                  _   
|  \/  (_) ___ _ __ ___  _ __   __ _ _   _| |_ 
| |\/| | |/ __| '__/ _ \| '_ \ / _` | | | | __|
| |  | | | (__| | | (_) | | | | (_| | |_| | |_ 
|_|  |_|_|\___|_|  \___/|_| |_|\__,_|\__,_|\__|
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
./mvnw package -Dpackaging=native-image -Pwasm-debug

# Run the app and enable the Chrome DevTools Protocol
./target/demo-wasm-debug -Dinspect=true

# Run the app and enable the Debug Adapter Protocol
./target/demo-wasm-debug -Ddap=true
```

Once you are in a debugging session, the debugger should break in the JavaScript binding.
You can resume execution, which will bring up the server, and set breakpoints, for example in `flipv()` or any other effect.
When you then run code that has breakpoints set, for example by clicking on "Flip Vertical" on the landing page, execution should halt at the breakpoint positions.
From there, you can step through the code, including stepping into calls to Wasm, which will reveal the Rust sources of Photon.
Be aware that on the Rust side, you may need to step into an additional frame injected by wasm-bindgen and over additional code inlined by the Rust compiler.
Check out these demo debugging sessions in [IntelliJ](https://youtu.be/YqrEqXB59rA?t=3057) and [VS Code](https://youtu.be/uefc2t9AmQI?t=2093) for more details.

Note that the debug build of Photon has additional optimizations disabled in the Rust toolchain, causing the application to perform more slowly compared with the release build.
Also note that the DWARF debug info increases the file size of the Photon Wasm module from ~1.5M to ~26M, and with that also the file size of the JARs and native images. 

## Implementation Details

The [`DemoController`](src/main/java/com/example/DemoController.java) uses a [`PhotonService`](src/main/java/com/example/PhotonService.java) to implement the `/photo/{effectName}` endpoint.
This service accesses `Photon` objects that are pooled in a [`PhotonPool`](src/main/java/com/example/PhotonPool.java) to check whether an effect for a given effectName exists before applying the effect to a sample image.

`PhotonPool` creates a `Photon` object for each available processor, which in turn holds a reference to the corresponding `photonModule` and an `imageContent` object.
Both of these objects are from JavaScript and backed by the same [`Context`](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Context.html).
Note that the `Context` objects share the same [`Engine`](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Engine.html), to improve warmup and memory footprint.

Also note that the Photon JavaScript and WebAssembly modules as well as the sample images are downloaded when the demo is built with the `wagon-maven-plugin` Maven plugin (see _pom.xml_).

The [`DemoTest`](src/test/java/com/example/DemoTest.java) tests that applying the same effect on two copies of the same image yields the same result, regardless of the effect used.
Run it with `./mvnw test`.
