# Embed Rust in Java Using GraalWasm

The example below demonstrates how to compile a Rust function to WebAssembly and run it embedded in a Java application.

### Prerequisites

To complete this guide, you need the following:
- [GraalVM JDK](https://www.graalvm.org/downloads/)
- [Rust](https://www.rust-lang.org/tools/install)
- [Maven](https://maven.apache.org/)

## 1. Setting up the Maven Project

To follow this guide, generate the application from the [Maven Quickstart Archetype](https://maven.apache.org/archetypes/maven-archetype-quickstart/):

```shell
mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DgroupId=com.example -DartifactId=demo -DinteractiveMode=false
```
```shell
cd demo
```

### 1.1. Adding the Polyglot API and GraalWasm Dependencies

The GraalVM Polyglot API can be easily added as a Maven dependency to your Java project.
The GraalWasm artifact should be on the Java module or class path too.

Add the following set of dependencies to the `<dependencies>` section of your project's _pom.xml_:

- To add the Polyglot API:
    ```xml
    <!-- <dependencies> -->
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>24.2.1</version>
    </dependency>
    <!-- </dependencies> -->
    ```
- To add GraalWasm:
    ```xml
    <!-- <dependencies> -->
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>wasm</artifactId>
        <version>24.2.1</version>
        <type>pom</type>
    </dependency>
    <!-- </dependencies> -->
    ```

## 2. Setting Up Rust Code

Next, Create a Rust project and then write a Rust function and compile it into a WebAssembly module.

### 2.1  Creating rust project
```BASH
cargo new hello-rust

```

### 2.2. Writing Rust Code

Put the following Go program in _hello-rust/src/lib.rs_:

```c
#[unsafe(no_mangle)]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
    a + b
}


```


### 2.3. Compiling Rust Code to WebAssembly

Enter the following command into your terminal:
```shell
rustup target add wasm32-wasip1

```
```shell
cd hello-rust
```
```shell
cargo build --target wasm32-wasip1
```


## 3. Using the WebAssembly Module from Java

Now you can embed this WebAssembly function in a Java application. Make sure to move your wasm file from hello-rust/target/wasm32-wasip1/release/hello-rust.wasm to your resources folder and then put the following in _src/main/java/com/example/App.java_:

```java
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;

public class App {
  public static void main(String[] args) throws IOException {
    Context context = Context.newBuilder("wasm").option("wasm.Builtins", "wasi_snapshot_preview1").build();
    URL wasmFile = App.class.getResource("/hello_rust.wasm");
    Source source = Source.newBuilder("wasm", wasmFile).build();
    Value wasmBindings = context.eval(source);
    Value add = wasmBindings.getMember("add");

    int result = add.execute(5, 7).asInt();
    System.out.println("5 + 7 = " + result);
  }
}
```

## 4. Building and Testing the Application

Compile and run this Java application with Maven:

```shell
mvw package
mvn exec:java -Dexec.mainClass=com.example.App
```

The expected output should contain:
```
5 + 7 = 12
```

## Conclusion

By following this guide, you have learned how to:
* Compile Rust code to a WebAssembly module and export Rust functions as WebAssembly exports.
* Load WebAssembly modules in Java using GraalWasm.
* Call functions exported from Rust in your Java application.

### Learn More

You can learn more at:
* [GraalWasm Reference Manual](https://www.graalvm.org/latest/reference-manual/wasm/)
* [GraalVM Embedding Languages Documentation](https://www.graalvm.org/jdk23/reference-manual/embed-languages/)
* [GraalWasm on GitHub](https://github.com/oracle/graal/tree/master/wasm)