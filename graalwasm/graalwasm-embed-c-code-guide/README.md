# Embed C in Java Using GraalWasm

The example below demonstrates how to compile a C function to WebAssembly and run it embedded in a Java application.

### Prerequisites

To complete this guide, you need the following:
- [GraalVM JDK](https://www.graalvm.org/downloads/)
- [Emscripten compiler frontend](https://emscripten.org/docs/tools_reference/emcc.html)
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
           <version>24.2.2</version>
       </dependency>
       <!-- </dependencies> -->
       ```
   - To add GraalWasm:
       ```xml
       <!-- <dependencies> -->
       <dependency>
           <groupId>org.graalvm.polyglot</groupId>
           <artifactId>wasm</artifactId>
           <version>24.2.2</version>
           <type>pom</type>
       </dependency>
       <!-- </dependencies> -->
       ```

## 2. Setting Up C Code

Next, write a C function and compile it into a WebAssembly module.

### 2.1. Writing C Code

Put the following C program in _src/main/c/floyd.c_:

```c
#include <stdio.h>

void floyd() {
    int number = 1;
    int rows = 10;
    for (int i = 1; i <= rows; i++) {
        for (int j = 1; j <= i; j++) {
            printf("%d ", number);
            ++number;
        }
        printf(".\n");
    }
}

int main() {
    floyd();
    return 0;
}
```

Note that `floyd` is defined as a separate function and can be exported.

### 2.2. Compiling C Code to WebAssembly

Compile the C code using the most recent version of the [Emscripten compiler frontend](https://emscripten.org/docs/tools_reference/emcc.html):

```shell
mkdir -p target/classes/com/example
```
```shell
emcc --no-entry -s EXPORTED_FUNCTIONS=_floyd -o target/classes/com/example/floyd.wasm src/main/c/floyd.c
```

> The exported functions must be prefixed by `_`. If you reference that function in the Java code, the exported name should not contain the underscore.

It produces a standalone file _floyd.wasm_ in _target/classes/com/example/_, which enables you to load the file as a resource.

#### Using Maven to Compile C Code

You can automate the C compilation and make it a part of the Maven build process by adding the following plugin configuration to the `<build>` section of the _pom.xml_ file.

```xml
<!-- <build> -->
<plugins>
  <plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>1.2.1</version>
    <executions>
      <execution>
        <id>create-output-directory</id>
        <phase>generate-resources</phase>
        <goals>
          <goal>exec</goal>
        </goals>
        <configuration>
          <executable>mkdir</executable>
          <commandlineArgs>-p ${project.build.outputDirectory}/com/example/</commandlineArgs>
        </configuration>
      </execution>
      <execution>
        <id>compile-c-into-wasm</id>
        <phase>generate-resources</phase>
        <goals>
          <goal>exec</goal>
        </goals>
        <configuration>
          <executable>emcc</executable>
          <commandlineArgs>--no-entry -s EXPORTED_FUNCTIONS=_floyd -o ${project.build.outputDirectory}/com/example/floyd.wasm ${project.basedir}/src/main/c/floyd.c</commandlineArgs>
        </configuration>
      </execution>
    </executions>
  </plugin>
</plugins>
<!-- </build> -->
```

This binds the `exec-maven-plugin:exec` goal to the `generate-resources` phase of the build lifecycle.
The `exec` goal runs `mkdir` and `emcc` with the same command line arguments as above, ensuring that the generated WebAssembly module file is included as a resource file in the final JAR file.

## 3. Using the WebAssembly Module from Java

Now you can embed this WebAssembly function in a Java application. Put the following in _src/main/java/com/example/App.java_:

```java
package com.example;

import java.io.IOException;
import java.net.URL;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class App {
   public static void main(String[] args) throws IOException {
      // Find the WebAssembly module resource
      URL wasmFile = App.class.getResource("floyd.wasm");

      // Setup context
      Context.Builder contextBuilder = Context.newBuilder("wasm").option("wasm.Builtins", "wasi_snapshot_preview1");
      Source.Builder sourceBuilder = Source.newBuilder("wasm", wasmFile).name("example");
      Source source = sourceBuilder.build();
      Context context = contextBuilder.build();

      // Evaluate the WebAssembly module
      context.eval(source);

      // Execute the floyd function
      context.getBindings("wasm").getMember("example").getMember("_initialize").executeVoid();
      Value mainFunction = context.getBindings("wasm").getMember("example").getMember("floyd");
      mainFunction.execute();
      context.close();
   }
}
```

## 4. Building and Testing the Application

Compile and run this Java application with Maven:

```shell
mvw package
mvn exec:java -Dexec.mainClass=com.example.App
```

The expected output should contain the first 10 lines of [Floyd's triangle](https://en.wikipedia.org/wiki/Floyd%27s_triangle), printed using the C function:

```
1 .
2 3 .
4 5 6 .
7 8 9 10 .
11 12 13 14 15 .
16 17 18 19 20 21 .
22 23 24 25 26 27 28 .
29 30 31 32 33 34 35 36 .
37 38 39 40 41 42 43 44 45 .
46 47 48 49 50 51 52 53 54 55 .
```

## Conclusion

By following this guide, you have learned how to:
* Compile C code to a WebAssembly module and export C functions as WebAssembly exports.
* Load WebAssembly modules in Java using GraalWasm.
* Call functions exported from C in your Java application.

### Learn More

You can learn more at:
* [GraalWasm Reference Manual](https://www.graalvm.org/latest/reference-manual/wasm/)
* [GraalVM Embedding Languages Documentation](https://www.graalvm.org/jdk23/reference-manual/embed-languages/)
* [GraalWasm on GitHub](https://github.com/oracle/graal/tree/master/wasm)
