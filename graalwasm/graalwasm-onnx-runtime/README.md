### ONNX-WASM on GraalVM

This project demonstrates how to use the onnxruntime-web (ONNX Runtime WebAssembly) package inside a Java application powered by GraalVM Polyglot features. It loads and runs ONNX models entirely in the browser or JVM using WebAssembly (WASM) and JavaScript bindings.



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
