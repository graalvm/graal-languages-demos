### ONNX-WASM on GraalVM

This project is a GraalVM-based reimplementation of the [ONNX Runtime Web Quick Start Demo](https://github.com/microsoft/onnxruntime-inference-examples/blob/main/js/quick-start_onnxruntime-web-script-tag/index.html)
provided by Microsoft.

It demonstrates how to run ONNX models using the onnxruntime-web package inside a Java application via GraalVM Polyglot features. The core idea is to evaluate ONNX models entirely in the browser or on the JVM using WebAssembly (WASM) and JavaScript, tightly integrated with Java code.


### Technologies Used

GraalVM (Java + JavaScript + WASM support)
onnxruntime-web (ONNX Runtime WASM backend)
Java (with Maven or Gradle)
Polyglot Context (GraalVM)
WASM + JavaScript bridge

### How It Works

Java loads a JavaScript wrapper (ort.js) that utilizes onnxruntime-web.
ONNX model is passed as a buffer or loaded inside JS using polyglot exports.
Inference is triggered through JavaScript and results returned to Java via Polyglot.
