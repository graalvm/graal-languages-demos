# Embed Go in Java Using GraalVM and JS

The example below demonstrates how to compile Go code to WebAssembly and embed it in a Java application using [GraalWasm](https://graalvm.org/webassembly).
To enable interoperability, generate JavaScript bindings for the Rust library and run them on [GraalJS](https://graalvm.org/javascript).

### Prerequisites

To complete this guide, you need the following:
- [Maven](https://maven.apache.org/)
- [Go](https://go.dev/) 1.25 or later
- JDK 21 or later (e.g., [GraalVM JDK](https://www.graalvm.org/downloads/))
- Your favorite IDE or text editor for coding comfortably 
- A bit of time to explore and experiment 

## 1. Setting Up the Maven Project

To follow this guide, generate the application from the [Maven Quickstart Archetype](https://maven.apache.org/archetypes/maven-archetype-quickstart/) and go into the directory:

```shell
mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DgroupId=com.example -DartifactId=demo -DinteractiveMode=false
cd demo
```

### 1.1. Add the Polyglot API and GraalWasm Dependencies

Add the following set of dependencies to the `<dependencies>` section of your project's _pom.xml_:

``` xml
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

## 2. Set Up Go Code

Next, create a Go project (e.g., _main.go_) and add Go code.

### 2.1  Create a Go project

```bash
mkdir src/main/go/
touch src/main/go/main.go
```

### 2.2. Write Go Code

Add the following Go program to _main.go_:

```go
package main

import "syscall/js"

func add(this js.Value, args []js.Value) interface{} {
    return args[0].Int() + args[1].Int()
}

func reverseString(this js.Value, args []js.Value) interface{} {
    if len(args) < 1 {
        return js.ValueOf("")
    }
    s := args[0].String()

    runes := []rune(s)
    for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
        runes[i], runes[j] = runes[j], runes[i]
    }
    reversed := string(runes)
    return js.ValueOf(reversed)
}

func registerMainModule() {
    main := js.Global().Get("Object").New()
    main.Set("add", js.FuncOf(add))
    main.Set("reverseString", js.FuncOf(reverseString))
    js.Global().Set("main", main)
}

func main() {
    wait := make(chan struct{}, 0)
    registerMainModule()
    <-wait
}
```


### 2.3. Compile Go to WebAssembly

To compile Go code to WebAssembly, ensure that the `go` compiler is installed and available on your system path.
Use the `exec-maven-plugin` to invoke `go` as part of the `generate-resources` Maven phase:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <executable>java</executable>
        <arguments>
            <argument>-classpath</argument>
            <classpath/>
            <argument>--enable-native-access=ALL-UNNAMED</argument>
            <argument>--sun-misc-unsafe-memory-access=allow</argument>
            <argument>com.example.App</argument>
        </arguments>
    </configuration>
    <executions>
        <execution>
            <id>build-go-wasm</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration combine.self="override">
                <executable>go</executable>
                <environmentVariables>
                    <GOOS>js</GOOS>
                    <GOARCH>wasm</GOARCH>
                </environmentVariables>
                <arguments>
                    <argument>build</argument>
                    <argument>-o</argument>
                    <argument>${project.build.outputDirectory}/go/main.wasm</argument>
                    <argument>src/main/go/main.go</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 3. Copy wasm_exec.js file into the target directory:

Running Go compiled to WebAssembly requires a _wasm_exec.js_ file with JavaScript glue code provided by the Go toolchain.
Add the following _exec-maven-plugin_ to your _pom.xml_ to automatically copy the _wasm_exec.js_ file into the target directory.

```xml
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-extra-resources</id>
            <phase>process-resources</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.outputDirectory}/go</outputDirectory>
                <resources>
                    <resource>
                        <directory>${env.GOROOT}/lib/wasm</directory>
                        <includes>
                            <include>wasm_exec.js</include>
                        </includes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Make sure that the `GOROOT` environment variable is set correctly before invoking Maven: 

```shell
export GOROOT=$(go env GOROOT)
```

## 4. Implement JavaScript Polyfill

By default, Go on Wasm is designed to run in the browser or on Node.js.
To run it on GraalJS, a polyfill for `crypto.getRandomValues()` is required.
This can be implemented using `SecureRandom` from the JDK:

```java
package com.example;

import org.graalvm.polyglot.Value;

import java.security.SecureRandom;

public class CryptoPolyfill {
    private final SecureRandom random = new SecureRandom();

    public Object getRandomValues(Value buffer) {
        if (!buffer.hasArrayElements()) {
            throw new IllegalArgumentException("TypeMismatchError: The data argument must be an integer-type TypedArray");
        }
        long arraySize = buffer.getArraySize();
        if (arraySize > 65536) {
            throw new IllegalArgumentException("QuotaExceededError: The requested length exceeds 65,536 bytes");
        }
        int size = Math.toIntExact(arraySize);
        byte[] randomBytes = new byte[size];
        random.nextBytes(randomBytes);
        for (int i = 0; i < size; i++) {
            buffer.setArrayElement(i, randomBytes[i]);
        }
        return buffer;
    }
}
```

##  5. Adding Go Interface in Java

To enable interoperability between Java and Go, add a Java interface to your project and use [`Value.as(Class)`](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/Value.html#as(java.lang.Class)) to program against it:

```java
package com.example;

interface GoMain {
    int add(int a, int b);

    String reverseString(String str);
}
```

## 7. Using the WebAssembly Module from Java.

Now you can embed the Wasm module in a Java application:

```java
package com.example;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class App {
    public static final String GO_MAIN_WASM = "/go/main.wasm";
    public static final String GO_WASM_EXEC = "/go/wasm_exec.js";

    public static void main(String[] args) throws IOException {
        // Load Go resources
        byte[] wasmBytes;
        try (InputStream in = App.class.getResourceAsStream(GO_MAIN_WASM)) {
            if (in == null) {
                throw new FileNotFoundException(GO_MAIN_WASM);
            }
            wasmBytes = in.readAllBytes();
        }
        URL wasmExecURL = App.class.getResource(GO_WASM_EXEC);
        if (wasmExecURL == null) {
            throw new FileNotFoundException(GO_WASM_EXEC);
        }
        // Create a context with Wasm and JavaScript access
        try (Context context = Context.newBuilder("js", "wasm")
                .option("js.performance", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true")
                .allowAllAccess(true)
                .build()) {
            // Install Wasm bytes and crypto polyfill in JS binding
            Value jsBindings = context.getBindings("js");
            jsBindings.putMember("wasmBytes", wasmBytes);
            jsBindings.putMember("crypto", new CryptoPolyfill());
            // Evaluate wasm_exec.js file
            context.eval(Source.newBuilder("js", wasmExecURL).build());
            // Instantiate the Wasm module and invoke go.run()
            context.eval("js", """
                    async function run(wasmBytes) {
                        const go = new Go();
                        const {instance} = await WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject);
                        go.run(instance);
                    }
                    run(wasmBytes);
                    """);
            // Access main module and interact with it through a Java interface
            GoMain goMain = jsBindings.getMember("main").as(GoMain.class);
            System.out.printf("3 + 4 = %s%n", goMain.add(3, 4));
            System.out.printf("reverseString('Hello World') = %s%n", goMain.reverseString("Hello World"));
        }
    }
}
```

## 8. Build and Test the Application

Compile and run this Java application with Maven:

```shell
export GOROOT=$(go env GOROOT)
./mvnw package
./mvnw exec:exec
```

The expected output should look like this:
```
3 + 4 = 7
reverseString('Hello World') = dlroW olleH
```
