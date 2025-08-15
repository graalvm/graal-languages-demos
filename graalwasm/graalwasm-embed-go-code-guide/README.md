# Embed Go in Java Using GraalVM and JS

The example below demonstrates how to compile a Go function to WebAssembly and run it embedded in a Java application.

### Prerequisites

To complete this guide, you need the following:
- [GraalVM JDK](https://www.graalvm.org/downloads/)
- [Go](https://go.dev/dl/)
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

``` xml
<!-- <dependencies> -->
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>polyglot</artifactId>
      <version>25.0.0-SNAPSHOT</version>
  </dependency>
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>wasm</artifactId>
      <version>25.0.0-SNAPSHOT</version>
      <type>pom</type>
  </dependency>
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>js</artifactId>
      <version>25.0.0-SNAPSHOT</version>
      <type>pom</type>
  </dependency>
<!-- </dependencies> -->
```

## 2. Setting Up Go Code

Next, Create a Go project (_main.go_) and then write a Go function .

### 2.1  Creating Go project
```BASH
mkdir demo/src/main/go/
touch demo/src/main/go/main.go

```

### 2.2. Writing Go Code

Put the following Go program in _main.go_:

```
// main.go
package main

import "syscall/js"

func add(this js.Value, args []js.Value) interface{} {
    return args[0].Int() + args[1].Int()
}

func main() {
    js.Global().Set("add", js.FuncOf(add))
    select {}
}


```


### 2.3. Compiling Go Code to WebAssembly

Add the following _exec-maven-plugin_ to your pom.xml to automatically compile your go function to Wasm during the build process.

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>build-go-wasm</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>go</executable>
                <environmentVariables>
                    <GOOS>js</GOOS>
                    <GOARCH>wasm</GOARCH>
                </environmentVariables>
                <arguments>
                    <argument>build</argument>
                    <argument>-o</argument>
                    <argument>src/main/resources/mainw.wasm</argument>
                    <argument>src/main/go/main.go</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```
## 3.Implement JS glue code

_wasm_exec.js_ is a JavaScript glue code file provided by the Go toolchain for running Go programs compiled to WebAssembly (WASM). By default, it is designed for browsers or Node.js and is not fully compatible with the GraalVM JavaScript environment. In this project, additional polyfills have been added to wasm_exec.js to provide missing functionality (such as crypto.getRandomValues and process.hrtime). With these polyfills, you can now use Go-generated WASM modules seamlessly in GraalVM.

## 4. Implement JS logic:
Add this in you main.js file inside your resources folder.

```JS 
async function main(wasmData) {
    try {
        // Polyfill for instantiateStreaming if needed
        if (!WebAssembly.instantiateStreaming) {
            WebAssembly.instantiateStreaming = async (sourcePromise, importObject) => {
                const source = await sourcePromise;
                return await WebAssembly.instantiate(source, importObject);
            };
        }

        const go = new Go();
        const { instance } = await WebAssembly.instantiate(
            new Uint8Array(wasmData),
            go.importObject
        );
        go.run(instance);
        console.log("Sum:", global.add(1, 2));
    } catch (err) {
        console.error("Error running WebAssembly:", err);
    }
}

main(wasmData);

```

## 5. Using the WebAssembly Module from Java

Now you can embed this WebAssembly function in a Java application. Make sure to move your wasm and _wasm_exec.js_ files to your resources folder _src/main/resources_ and then put the following in _src/main/java/com/example/App.java_:

```java
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class App {
  public static void main(String[] args) throws IOException {
    Context context = Context.newBuilder("js","wasm")
            .option("js.webassembly", "true")
            .option("js.commonjs-require", "true")
            .allowAllAccess(true)
            .option("js.text-encoding","true").build();
    byte [] wasmData = Files.readAllBytes(Path.of("src/main/resources/main.wasm"));
    context.getBindings("js").putMember("wasmData",wasmData);
    context.eval(Source.newBuilder("js",App.class.getResource("/wasm_exec.js")).build());
    context.eval(Source.newBuilder("js",App.class.getResource("/main.js")).build());

  }
}
```

## 6. Building and Testing the Application

Compile and run this Java application with Maven:

```shell
mvn package
mvn exec:java -Dexec.mainClass=com.example.App
```

The expected output should contain
```
Sum: 3
```

## Conclusion

By following this guide, you have learned how to:
* Compile Go code to a WebAssembly module and export Go functions as WebAssembly exports.
* Load WebAssembly modules in Java using GraalWasm.
* Call functions exported from Go in your Java application.

### Learn More

You can learn more at:
* [GraalWasm Reference Manual](https://www.graalvm.org/latest/reference-manual/wasm/)
* [GraalVM Embedding Languages Documentation](https://www.graalvm.org/jdk23/reference-manual/embed-languages/)
* [GraalWasm on GitHub](https://github.com/oracle/graal/tree/master/wasm)