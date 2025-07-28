# Embed GO in Java Using GraalWasm

The example below demonstrates how to compile a Go function to WebAssembly and run it embedded in a Java application.

### Prerequisites

To complete this guide, you need the following:
- [GraalVM JDK](https://www.graalvm.org/downloads/)
- [tinygo](https://tinygo.org/getting-started/install/)
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

## 2. Setting Up Go Code

Next, Create a Go project (_main.go_) and then write a Go function and compile it into a WebAssembly module.

### 2.1  Creating GO project
```BASH
touch main.go

```

### 2.2. Writing GO Code

Put the following GO program in _main.go_:

```
package main

import (
    "fmt"
)
//export foo
func foo() {
    fmt.Println("Hello from Go!!")
}

func main(){
}


```


### 2.3. Compiling GO Code to WebAssembly

Enter the following command into your terminal:
```shell
tinygo build -o main.wasm -target=wasi main.go
```


## 3. Using the WebAssembly Module from Java

Now you can embed this WebAssembly function in a Java application. Make sure to move your wasm to your resources folder _src/main/resources_ and then put the following in _src/main/java/com/example/App.java_:

```java
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.net.URL;

public class App {
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("wasm")
                .option("wasm.Builtins", "wasi_snapshot_preview1")
                .build();
        URL wasmFile = App.class.getResource("/main.wasm");
        Source source = Source.newBuilder("wasm",wasmFile).build();
        Value wasmBindings = context.eval( source);

        Value main = wasmBindings.getMember("foo");
        main.execute();
    }
}
```

## 4. Building and Testing the Application

Compile and run this Java application with Maven:

```shell
mvn package
mvn exec:java -Dexec.mainClass=com.example.App
```

The expected output should contain
```
Hello from Go!
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