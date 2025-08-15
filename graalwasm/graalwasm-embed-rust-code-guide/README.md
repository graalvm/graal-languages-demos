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

### 2.1  Creating Rust project
```BASH
cargo new --lib src/main/mywasmlib 

```

### 2.2. Writing Rust Code

Put the following  program in _mywasmlib/src/lib.rs_:

```c
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
    #[wasm_bindgen]
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
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>build-rust</id>
            <phase>compile</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>wasm-pack</executable>
                <workingDirectory>${project.basedir}/src/main/mywasmlib</workingDirectory>
                <arguments>
                    <argument>build</argument>
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
## 3. Using the WebAssembly Module from Java using JS glue code

### 3.1 Creating Java main class.
Now you can embed Rust functions in a Java application.
```java
public class App {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Context context = Context.newBuilder("js", "wasm")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .option("js.webassembly", "true")
                .option("js.text-encoding", "true").build();
        URL myWasmLibURL = App.class.getResource("/mywasmlib/mywasmlib.js");
        Source jsSource = Source.newBuilder("js", myWasmLibURL).mimeType("application/javascript+module").build();
        MyWasmLib myWasmLibModule = context.eval(jsSource).as(MyWasmLib.class);
        System.out.println(myWasmLibModule.add(2, 3));
        System.out.println(myWasmLibModule.new_person("Anwar").say_hello());
        System.out.println(myWasmLibModule.reverse_string("Hello There!"));
    }

    interface MyWasmLib {
        int add(int left, int right);

        Person new_person(String name);

        interface Person {
            String say_hello();
        }
        String  reverse_string (String word);
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
Hello, Anwar!
!erehT olleH

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