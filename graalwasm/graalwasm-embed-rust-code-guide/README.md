# Embed Rust in Java Using GraalWasm and JS

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

```xml
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

## 2. Setting Up Rust Code

Next, Create a Rust project and then write a Rust function and compile it into a WebAssembly module.

### 2.1  Creating rust project
```BASH
cargo new --lib mywasmlib 

```

### 2.2. Writing Rust Code

Put the following Go program in _mywasmlib/src/lib.rs_:

```c
use wasm_bindgen::prelude::*;

#[wasm_bindgen]
pub fn add(left: i32, right: i32) -> i32 {
    left + right
}



```

Make sure your _Cargo.toml_ looks like this :

```declarative
[package]
name = "mywasmlib"
version = "0.1.0"
edition = "2024"

[dependencies]
wasm-bindgen = "0.2"

[lib]
crate-type = ["cdylib", "rlib"]
```


### 2.3. Compiling Rust Code to WebAssembly

This configuration runs the Rust wasm-pack build command automatically during the Maven build process. It uses the Maven Exec Plugin to compile the Rust library in the mywasmlib directory to WebAssembly, ensuring the WASM and JavaScript bindings are always up-to-date whenever you build the project with Maven.
```xml


        <executions>
          <execution>
            <id>build-rust</id>
            <phase>compile</phase>
            <goals>
              <goal>exec</goal>
            </goals>
            <configuration>
              <executable>wasm-pack</executable>
              <workingDirectory>${project.basedir}/mywasmlib</workingDirectory>
              <arguments>
                <argument>build</argument>
                <argument>--target</argument>
                <argument>bundler</argument>
                <argument>--out-dir</argument>
                <argument>../target</argument>
              </arguments>
            </configuration>
          </execution>
        </executions>

```
## 3. Using the WebAssembly Module from Java using JS glue code

### 3.1 . Using Rust-Generated JS Glue Code with Maven
When you compile a Rust library to WebAssembly (WASM) using wasm-bindgen and wasm-pack, both the .wasm binary and the appropriate JavaScript glue code are generated for you. Instead of writing custom glue code, this project simply reuses the JS file created by wasm-pack \
To use them in your project , first add the following exec-plugin in your pom.xml :
```xml
<plugin>
    <artifactId>maven-antrun-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <phase>process-resources</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <target>
                    <copy todir="${project.build.directory}">
                        <fileset dir="src/main/js" includes="**/*.js"/>
                  </copy>
                </target>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Next, we need to add the following wasm binding code in your _src/main/js/main.js_ file:
```
import * as wasm from "./mywasmlib.js";
console.log(wasm.add(1,3))

```


### 3.2 Creating Java main class.
Now you can embed this WebAssembly function in a Java application.
```java
public class App {
    public static void main(String[] args) throws IOException {
        Context context = Context.newBuilder("js","wasm")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.webassembly", "true")
                .option("js.text-encoding","true").build();
        Path jsFilePath = Paths.get("target", "main.js");
        Source jsSource = Source.newBuilder("js", jsFilePath.toFile()).mimeType("application/javascript+module").build();
        context.eval(jsSource);
    }
}

```

## 4. Building and Testing the Application

Compile and run this Java application with Maven:

```shell
mvn package
mvn exec:java -Dexec.mainClass=com.example.App
```

The expected output should contain:
```
5
```

## Conclusion

By following this guide, you have learned how to:
* Compile Rust code to a WebAssembly module and export Rust functions as WebAssembly exports.
* Load WebAssembly modules in Java using GraalWasm and Javascript.
* Call functions exported from Rust in your Java application.

### Learn More

You can learn more at:
* [GraalWasm Reference Manual](https://www.graalvm.org/latest/reference-manual/wasm/)
* [GraalVM Embedding Languages Documentation](https://www.graalvm.org/jdk23/reference-manual/embed-languages/)
* [GraalWasm on GitHub](https://github.com/oracle/graal/tree/master/wasm)