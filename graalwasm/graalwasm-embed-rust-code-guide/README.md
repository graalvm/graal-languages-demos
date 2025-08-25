# Embed Rust in Java Using GraalWasm

The example below demonstrates how to compile a Rust library to WebAssembly and embed it in a Java application using [GraalWasm](https://graalvm.org/webassembly).
To enable interoperability, generate JavaScript bindings for the Rust library and run them on [GraalJS](https://graalvm.org/javascript).

### Prerequisites

To complete this guide, you need the following:
- [Maven](https://maven.apache.org/)
- [`wasm-pack`](https://drager.github.io/wasm-pack/installer/)
- JDK 21 or later
- Your favorite IDE or text editor for coding comfortably 
- A bit of time to explore and experiment 

## 1. Setting Up the Maven Project

To follow this guide, generate the application from the [Maven Quickstart Archetype](https://maven.apache.org/archetypes/maven-archetype-quickstart/) and go into the directory:

```shell
mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DgroupId=com.example -DartifactId=demo -DinteractiveMode=false
cd demo
```

### 1.1. Adding the Polyglot API and GraalWasm Dependencies

Add the following set of dependencies to the `<dependencies>` section of your project's _pom.xml_:

```xml
<!-- <dependencies> -->
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>polyglot</artifactId>
      <version>${graal.languages.version}</version>
  </dependency>
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>wasm</artifactId>
      <version>${graal.languages.version}</version>
      <type>pom</type>
  </dependency>
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>js</artifactId>
      <version>${graal.languages.version}</version>
      <type>pom</type>
  </dependency>
<!-- </dependencies> -->
```

## 2. Setting Up Rust Code

Next, create a Rust project, write Rust code, and compile it into a WebAssembly module.

### 2.1  Creating the Rust project
```bash
cargo new --lib src/main/rust/mywasmlib 

```

### 2.2. Writing Rust Code

Add the following program to _mywasmlib/src/lib.rs_:

```rust
use wasm_bindgen::prelude::*;

#[wasm_bindgen]
pub fn add(left: i32, right: i32) -> i32 {
    left + right
}

#[wasm_bindgen]
pub struct Person {
    name: String,
}

#[wasm_bindgen]
impl Person {
    pub fn say_hello(&self) -> String {
        format!("Hello, {}!", self.name)
    }
}

#[wasm_bindgen]
pub fn new_person(name: String) -> Person {
    Person { name }
}

#[wasm_bindgen]
pub fn reverse_string(input: String) -> String {
    input.chars().rev().collect()
}
```

Make sure your _Cargo.toml_ looks like this :

```toml
[package]
name = "mywasmlib"
version = "0.1.0"
edition = "2024"

[dependencies]
wasm-bindgen = "0.2"

[lib]
crate-type = ["cdylib", "rlib"]
```


### 2.3. Compiling Rust to WebAssembly

To compile the Rust library to WebAssembly, ensure `wasm-pack` is installed and available on your system path.
Use the `exec-maven-plugin` to invoke `wasm-pack` as part of the `generate-resources` Maven phase:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <!--default configuration-->
    <executions>
        <execution>
            <id>build-rust</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>wasm-pack</executable>
                <workingDirectory>${project.basedir}/src/main/rust/mywasmlib</workingDirectory>
                <arguments combine.self="override">
                    <argument>build</argument>
                    <argument>--no-typescript</argument>
                    <argument>--target</argument>
                    <argument>bundler</argument>
                    <argument>--out-dir</argument>
                    <argument>${project.build.outputDirectory}/mywasmlib</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

As a result, the Wasm module becomes available as a Java resource on the class path.
Note that `wasm-pack` also creates a _target/_ directory in _src/main/rust/mywasmlib_, which you want to add to your `.gitignore`.

## 3. Using the WebAssembly Module from Java using the JavaScript Binding

### 3.1 Creating Java main class.

Now you can embed the Rust library in a Java application using the GraalVM Polyglot API.
To do this:
1. Load the JavaScript binding as a Java resource
2. Evaluate the binding as a JavaScript module in a `Context` with access to `js` and `wasm`
3. Create a Java interface for the Rust library
3. Use [`Value.as(Class)`](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Value.html#as(java.lang.Class)) to expose the module under the Java interface
4. Program against the Java interface as usual 

```java
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;


public class App {
    private static final String MYWASMLIB_JS_RESOURCE = "/mywasmlib/mywasmlib.js";

    public static void main(String[] args) throws IOException {
        URL myWasmLibURL = App.class.getResource(MYWASMLIB_JS_RESOURCE);
        if (myWasmLibURL == null) {
            throw new FileNotFoundException(MYWASMLIB_JS_RESOURCE);
        }
        try (Context context = Context.newBuilder("js", "wasm")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true").build()) {
            Source jsSource = Source.newBuilder("js", myWasmLibURL).mimeType("application/javascript+module").build();
            MyWasmLib myWasmLibModule = context.eval(jsSource).as(MyWasmLib.class);

            System.out.println(myWasmLibModule.add(2, 3));
            System.out.println(myWasmLibModule.new_person("Jane").say_hello());
            System.out.println(myWasmLibModule.reverse_string("Hello There!"));
        }
    }

    interface MyWasmLib {
        int add(int left, int right);

        interface Person {
            String say_hello();
        }

        Person new_person(String name);

        String reverse_string(String word);
    }
}
```

## 4. Building and Testing the Application

Compile and run the Java application with Maven:

```shell
./mvnw package
./mvnw exec:exec
```

The expected output should look like this:
```
5
Hello, Jane!
!erehT olleH
```

## Compiling the Application to Native

With GraalVM Native Image, this Java application can be compiled into a native executable that starts instantly, scales fast, and uses fewer compute resources.
For this, you need additional [reachability metadata](https://www.graalvm.org/latest/reference-manual/native-image/metadata/) to register reflection, proxies, and resources.
You can find the corresponding configuration for this Java application in [_reachability-metadata.json_](src/META-INF/native-image/com.example/app/reachability-metadata.json).

Afterward, add a new profile using the `native-maven-plugin` to your _pom.xml_:

```xml
    <profiles>
        <profile>
            <id>native</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.graalvm.buildtools</groupId>
                        <artifactId>native-maven-plugin</artifactId>
                        <extensions>true</extensions>
                        <executions>
                            <execution>
                                <id>build-native</id>
                                <goals>
                                    <goal>compile-no-fork</goal>
                                </goals>
                                <phase>package</phase>
                            </execution>
                        </executions>
                        <configuration>
                            <mainClass>com.example.App</mainClass>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
```

To build the native executable, run:

```bash
./mvnw -Pnative package
```

Finally, you can run the native executable:

```bash
./target/demo
```

The output should be the same (see [here](#4-building-and-testing-the-application)), only the application starts and runs much faster and requires fewer CPU and memory resources.
