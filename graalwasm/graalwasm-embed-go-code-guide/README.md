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
      <version>24.2.2</version>
  </dependency>
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>wasm</artifactId>
      <version>24.2.2</version>
      <type>pom</type>
  </dependency>
  <dependency>
      <groupId>org.graalvm.polyglot</groupId>
      <artifactId>js</artifactId>
      <version>24.2.2</version>
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


### 2.3. Compiling Go Code to WebAssembly

Add the following _exec-maven-plugin_ to your pom.xml to automatically compile your go function to Wasm during the build process.

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
## 3.Implement JS glue code

_wasm_exec.js_ is a JavaScript glue code file provided by the Go toolchain for running Go programs compiled to WebAssembly (WASM). By default, it is designed for browsers or Node.js and is not fully compatible with the GraalVM JavaScript environment. In this project, additional polyfills have been added to wasm_exec.js to provide missing functionality (such as crypto.getRandomValues and process.hrtime). With these polyfills, you can now use Go-generated WASM modules seamlessly in GraalVM.

## 4. Copy wasm_exec.js file into the target directory:
Add the following _exec-maven-plugin_ to your pom.xml to automatically copy the _wasm_exec.js_ file into the target directory .

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
Make sure that GOROOT is Set for Maven: 
```shell
export GOROOT=$(go env GOROOT)
```
## 5. Adding the missing Polyfills.

To be able to run Go code within a Java application, we need to add a Crypto polyfill. To achieve this, we’ll create the following class:
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

##  6. Adding Go interface in java.
The GoMain interface defines methods that map to corresponding Go functions. By implementing this interface, the Java application can invoke Go functionality (such as add and reverseString) directly from Java code. This setup enables seamless integration between Java and Go components.
```java
package com.example;

interface GoMain {
    int add(int a, int b);

    String reverseString(String str);
}

```
## 7. Using the WebAssembly Module from Java.
Now you can embed this WebAssembly function in a Java application.
```java
public class App {
    public static final String GO_MAIN_WASM = "/go/main.wasm";
    public static final String GO_WASM_EXEC = "/go/wasm_exec.js";

    public static void main(String[] args) throws IOException, URISyntaxException {
        URL mainWasmURL = getResource(GO_MAIN_WASM);
        byte[] wasmBytes = Files.readAllBytes(Path.of(mainWasmURL.toURI()));
        URL wasmExecURL = getResource(GO_WASM_EXEC);
        try (Context context = Context.newBuilder("js", "wasm")
                .option("js.performance", "true")
                .option("js.text-encoding", "true")
                .option("js.webassembly", "true")
                .allowAllAccess(true)
                .build()) {
            Value jsBindings = context.getBindings("js");
            jsBindings.putMember("wasmBytes", wasmBytes);
            jsBindings.putMember("crypto", new CryptoPolyfill());
            context.eval(Source.newBuilder("js", wasmExecURL).build());
            context.eval("js", """
                    async function main(wasmBytes) {
                        const go = new Go();
                        const {instance} = await WebAssembly.instantiate(new Uint8Array(wasmBytes), go.importObject);
                        go.run(instance);
                    }
                    main(wasmBytes);
                    """);
            GoMain goMain = jsBindings.getMember("main").as(GoMain.class);
            System.out.printf("3 + 4 = %s%n", goMain.add(3, 4));
            System.out.printf("reverseString('Hello World') = %s%n", goMain.reverseString("Hello World"));
        }
    }

    private static URL getResource(String name) throws FileNotFoundException {
        URL url = App.class.getResource(name);
        if (url == null) {
            throw new FileNotFoundException(GO_MAIN_WASM);
        }
        return url;
    }
}
```

## 8. Building and Testing the Application

Compile and run this Java application with Maven:

```shell
mvn package
mvn exec:java -Dexec.mainClass=com.example.App
```

The expected output should contain
```
3 + 4 = 7
reverseString('Hello World') = dlroW olleH
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