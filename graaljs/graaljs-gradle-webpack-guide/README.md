# Using Node Packages in a Java Application with Gradle

## Introduction
This guide demonstrates how to integrate JavaScript libraries into a Java application using **GraalJS** and the **GraalVM Polyglot API**. Specifically, we will use the `qrcode` NPM package to generate QR codes within a Java application, leveraging GraalVM's ability to execute JavaScript code.

## Prerequisites
Before starting, ensure you have the following installed:
- **JDK 21** or later
- **Gradle 7.0** or later
- A text editor or IDE of your choice

## Installation

### Step 1: Clone the Repository
Clone the project repository to your local machine:

```sh
git clone https://github.com/graalvm/graal-languages-demos.git
cd graaljs/graaljs-gradle-webpack-guide
```

### Step 2: Compile and Run the Java Application
To build and run the Java application, use the following Gradle commands:

1. **Build the application**:

   ```sh
   gradle build
   ```

2. **Run the application**:

   ```sh
   gradle run --args="https://www.graalvm.org/"
   ```

   After running the application, you should see the generated QR code printed as output.

## Java Code Overview

### `App.java`
This is the main class that initializes the GraalVM context and executes the JavaScript QR code generation. The code integrates the `qrcode` NPM package by loading the JavaScript bundle (`bundle.mjs`) and calling the QR code generation function.

```java
try (Context context = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .option("engine.WarnInterpreterOnly", "false")
        .option("js.esm-eval-returns-exports", "true")
        .option("js.unhandled-rejections", "throw")
        .build()) {

    // Load the JavaScript bundle
    Source bundleSrc = Source.newBuilder("js", App.class.getResource("/bundle/bundle.mjs")).build();
    Value exports = context.eval(bundleSrc);

    // Set the input URL for QR code generation
    String input = args.length > 0 ? args[0] : "https://www.graalvm.org/javascript/";

    // Get QRCode object and generate QR code as a string
    QRCode qrCode = exports.getMember("QRCode").as(QRCode.class);
    Promise resultPromise = qrCode.toString(input);

    resultPromise.then((result) -> {
        System.out.println("Successfully generated QR code for \"" + input + "\".");
        System.out.println(result.asString());
    });
}
```

### `QRCode.java`
```java
public interface QRCode {
    Promise toString(String data);
    Promise toDataURL(String data, Object options);
}
```

### `Promise.java`
```java
public interface Promise {
    Promise then(ValueConsumer onResolve);
    Promise then(ValueConsumer onResolve, ValueConsumer onReject);
}
```

## JavaScript Code Overview

### `main.mjs`
```js
import 'fast-text-encoding';
export * as QRCode from 'qrcode';
```

### Webpack Configuration

#### `webpack.config.js`
```js
const path = require('path');
const { EnvironmentPlugin } = require('webpack');

const isProduction = process.env.NODE_ENV == 'production';

const config = {
    entry: './main.mjs',
    output: {
        path: process.env.BUILD_DIR
            ? path.resolve(process.env.BUILD_DIR)
            : path.resolve(__dirname, '../../build/classes/java/main/bundle'),
        filename: 'bundle.mjs',
        module: true,
        library: {
            type: 'module',
        },
        globalObject: 'globalThis'
    },
    experiments: {
        outputModule: true
    },
    mode: isProduction ? 'production' : 'development',
};

module.exports = () => config;
```

## Gradle Configuration

#### `build.gradle`
This Gradle configuration sets up the Java build environment, including the necessary dependencies for GraalVM and Node.js.

```gradle
plugins {
    id 'java'
    id 'application'
    id 'com.github.node-gradle.node' version '7.0.1'
}

group = 'org.example'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

application {
    mainClass = 'org.example.App'
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation 'org.graalvm.polyglot:polyglot:24.2.0'
    implementation 'org.graalvm.polyglot:js:24.2.0'
    
    testImplementation platform('org.junit:junit-bom:5.11.0')
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testImplementation 'org.junit.jupiter:junit-jupiter-params'
}

test {
    useJUnitPlatform()
}

node {
    version = '22.14.0'
    npmVersion = '10.9.2'
    download = true
    workDir = file("${project.buildDir}/node")
    npmWorkDir = file("${project.buildDir}/npm")
    nodeProjectDir = file('src/main/js')
}

tasks.register('webpackBuild', NpmTask) {
    dependsOn tasks.npmInstall
    workingDir = file('src/main/js')
    args = ['run', 'build']
    environment = ['BUILD_DIR': "${buildDir}/classes/java/main/bundle"]
}

processResources.dependsOn tasks.webpackBuild
```

## Conclusion
This project showcases how to integrate a Node.js package (`qrcode`) with a Java application using GraalVM's Polyglot API. By leveraging GraalJS, you can run JavaScript code within your Java application, allowing you to easily use JavaScript libraries like `qrcode`. With Gradle managing dependencies and Webpack bundling the JavaScript code, this approach enables seamless integration between Java and Node.js technologies.
