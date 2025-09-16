# Use Node Packages in a Java Application

> This guide shows how to use NPM packages in a Java application with GraalJS, using either **Gradle** or **Maven** as your build tool.

- [Introduction](#introduction)
- [Prerequisites](#prerequisites)
- [1. Set Up the Java Project](#1-set-up-the-java-project)
  - [Create a New Gradle Java Project](#create-a-new-gradle-java-project)
  - [Create a New Maven Java Project](#create-a-new-maven-java-project)
- [2. Set Up the JavaScript Build](#2-set-up-the-javascript-build)
- [3. Use the JavaScript Library from Java](#3-use-the-javascript-library-from-java)
- [4. Build and Run the Application](#4-build-and-run-the-application)
- [Conclusion](#conclusion)

## Introduction

JavaScript libraries can be integrated into standard Java applications using [GraalJS](https://www.graalvm.org/javascript), which enables Java to execute JavaScript code.
You can do this with the [GraalVM Polyglot API](https://www.graalvm.org/latest/reference-manual/embed-languages/), which allows you to embed and interact with JavaScript directly from Java code.

GraalJS is available as both [Gradle](https://www.graalvm.org/javascript) and [Maven](https://central.sonatype.com/artifact/org.graalvm.polyglot/js) artifacts.
This makes it easy to add GraalJS to your project regardless of the build tool.
Using Node (NPM) packages in Java projects often requires a bit more setup, due to the nature of the Node packaging ecosystem.
One way to use such modules is to prepackage them into a single _.js_ or _.mjs_ file using a bundler like [webpack](https://webpack.js.org/).

In this guide, you will integrate the `webpack` build into your Java project and embed the generated JavaScript code in your application's JAR file.

## Prerequisites

This guide demonstrates how to add the [qrcode](https://www.npmjs.com/package/qrcode) NPM package to a Java application to generate QR codes.

You will need:

- JDK 21 or later
- Gradle 8.0 or later, or Maven 3.6.3 or later
- A decent text editor or IDE

## 1. Set Up the Java Project

You can use either **Gradle** or **Maven** to set up your Java project:

### Create a New Gradle Java Project

Run the following command to create a new project with Gradle:

```shell
gradle init --type java-application
```

#### Add the GraalJS Dependencies

Add the following required GraalJS dependencies to the `dependencies` block of your _build.gradle_ file:

_build.gradle_

```gradle
dependencies {
    implementation 'org.graalvm.polyglot:polyglot:25.0.0' // ①
    implementation 'org.graalvm.polyglot:js:25.0.0'       // ②
}
```

❶ The `polyglot` dependency provides the APIs to manage and use GraalJS from Java.

❷ The `js` dependency is a meta-package that transitively depends on all libraries and resources to run GraalJS.

#### Add the Gradle Node Plugin

You can install most JavaScript packages from registries like [NPM](https://www.npmjs.com/) or [JSR](https://jsr.io/) using the `npm` package manager.
The Node.js ecosystem has conventions about the filesystem layout of installed packages that you need to keep in mind when embedding into Java.
Use a bundler to repackage all dependencies into a single file and simplify integration.
You can use the [`com.github.node-gradle.node`](https://github.com/node-gradle/gradle-node-plugin) plugin to manage the download, installation, and bundling for you.

Configure the Gradle Node plugin in the _build.gradle_ file with the following:

_build.gradle_

```gradle
plugins {
    id 'java'
    id 'application'
    id 'com.github.node-gradle.node' version '7.0.1' // ①
}

node { // ②
    version = '22.14.0'
    npmVersion = '10.9.2'
    download = true
    workDir = file("<span class="math-inline">\{project\.buildDir\}/node"\)
    npmWorkDir \= file\("</span>{project.buildDir}/npm")
    nodeProjectDir = file('src/main/js')
}

tasks.register('webpackBuild', NpmTask) { // ③
    dependsOn tasks.npmInstall
    workingDir = file('src/main/js')
    args = ['run', 'build']
    environment = ['BUILD_DIR': "${buildDir}/classes/java/main/bundle"]
}

processResources.dependsOn tasks.webpackBuild // ④
```

❶ Applies the node-gradle plugin, enabling Node.js and npm integration.

❷ Configures Node.js and npm versions, download settings, and working directories.

❸ Registers a `webpackBuild` task to run `npm run build` in the frontend directory. It ensures dependencies are installed first and sets the output directory.

❹ Ensures that the `webpackBuild` task is executed before the processResources task, so the bundled JavaScript is included in your JAR file.

### Create a New Maven Java Project

You can start with any Maven application that runs on JDK 21 or newer.
To follow this guide, generating the application from the [Maven Quickstart Archetype](https://maven.apache.org/archetypes/maven-archetype-quickstart/) is sufficient:

```shell
mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DgroupId=com.example -DartifactId=qrdemo -DinteractiveMode=false
cd qrdemo
```

#### Add the GraalJS Dependencies

Add the following required GraalJS dependencies to the `<dependencies>` section of your POM file:

_pom.xml_

```xml
<!-- <dependencies> -->
<dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>polyglot</artifactId> <!-- ① -->
    <version>25.0.0</version>
</dependency>

<dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>js</artifactId> <!-- ② -->
    <version>25.0.0</version>
    <type>pom</type> <!-- ③ -->
</dependency>
<!-- </dependencies> -->
```

❶ The `polyglot` dependency provides the APIs to manage and use GraalJS from Java.

❷ The `js` dependency is a meta-package that transitively depends on all libraries and resources to run GraalJS.

❸ Note that the `js` package is not a JAR - it is simply a POM that declares more dependencies.

#### Add the Maven Frontend Plugin

You can install most JavaScript packages from registries like [NPM](https://www.npmjs.com/) or [JSR](https://jsr.io/) using the `npm` package manager.
The Node.js ecosystem has conventions about the filesystem layout of installed packages that you need to keep in mind when embedding into Java.
Use a bundler to repackage all dependencies into a single file and simplify integration.
You can use the [`frontend-maven-plugin`](https://github.com/eirslett/frontend-maven-plugin) plugin to manage the download, installation, and bundling for you.

Configure the Maven Frontend plugin in your _pom.xml_ file to automate Node.js and JavaScript bundling steps:

_pom.xml_

```xml
<!-- <build> -->
<plugins>
    <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>1.15.0</version>

        <configuration>
            <nodeVersion>v21.7.2</nodeVersion>
            <workingDirectory>src/main/js</workingDirectory>
            <installDirectory>target</installDirectory>
        </configuration>

        <executions>
            <execution>
                <!-- ① -->
                <id>install node and npm</id>
                <goals><goal>install-node-and-npm</goal></goals>
            </execution>

            <execution>
                <!-- ② -->
                <id>npm install</id>
                <goals><goal>npm</goal></goals>
            </execution>

            <execution>
                <!-- ③ -->
                <id>webpack build</id>
                <goals><goal>webpack</goal></goals>
                <configuration>
                    <arguments>--mode production</arguments>
                    <environmentVariables>
                        <BUILD_DIR>${project.build.outputDirectory}/bundle</BUILD_DIR>
                    </environmentVariables>
                </configuration>
            </execution>
        </executions>
    </plugin>
</plugins>
<!-- </build> -->
```

❶ Installs `node` and `npm`.

❷ Runs `npm install` to download and install all NPM packages in _src/main/js_.

❸ Runs `webpack` to build a bundle of the JS sources in _target/classes/bundle_, which will be later included in the application's JAR file and can be loaded as a resource.

## 2. Set Up the JavaScript Build

Create the directory for your JavaScript sources:

```shell
mkdir -p src/main/js
cd src/main/js
```

You can manually set up the build environment with these steps:

1. Run `npm init` and follow the instructions (package name: "qrdemo", entry point: "main.mjs").
2. Run `npm install -D @webpack-cli/generators`.
3. Run `npx webpack-cli init` and follow the instructions (select "ES6" and "npm").
4. Run `npm install --save qrcode` to install and add the `qrcode` dependency.
5. Run `npm install --save assert util stream-browserify browserify-zlib fast-text-encoding` to install the polyfill packages to build with the webpack configuration below.

Alternatively, you can use the following _package.json_ file to define your dependencies and build scripts:

_package.json_

```json
{
  "name": "qrdemo",
  "version": "1.0.0",
  "description": "QRCode demo app",
  "main": "main.mjs",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1",
    "build": "webpack --mode=production --node-env=production",
    "build:dev": "webpack --mode=development",
    "build:prod": "webpack --mode=production --node-env=production",
    "watch": "webpack --watch"
  },
  "dependencies": {
    "assert": "^2.1.0",
    "browserify-zlib": "^0.2.0",
    "fast-text-encoding": "^1.0.6",
    "qrcode": "^1.5.4",
    "stream-browserify": "^3.0.0",
    "util": "^0.12.5"
  },
  "devDependencies": {
    "@babel/core": "^7.25.2",
    "@babel/preset-env": "^7.25.4",
    "@webpack-cli/generators": "^3.0.7",
    "babel-loader": "^9.1.3",
    "webpack": "^5.94.0",
    "webpack-cli": "^5.1.4"
  }
}
```

Create a _webpack.config.js_ file, or open the one created by `webpack-cli init`, and fill it with the following contents:

_webpack.config.js_

```js
const path = require('path');
const { EnvironmentPlugin } = require('webpack');

const config = {
    entry: './main.mjs',
    output: {
        path: path.resolve(process.env.BUILD_DIR),
        filename: 'bundle.mjs',
        module: true,
        library: {
            type: 'module',
        },
        globalObject: 'globalThis'
    },
    experiments: {
        outputModule: true // Generate ES module sources
    },
    optimization: {
        usedExports: true, // Include only used exports in the bundle
        minimize: false,   // Disable minification
    },
    resolve: {
        aliasFields: [],   // Disable browser alias to use the server version of the qrcode package
        fallback: {        // Redirect Node.js core modules to polyfills
            "stream": require.resolve("stream-browserify"),
            "zlib": require.resolve("browserify-zlib"),
            "fs": false    // Exclude the fs module altogether
        },
    },
    plugins: [
        new EnvironmentPlugin({
            NODE_DEBUG: false, // Set process.env.NODE_DEBUG to false
        }),
    ],
};

module.exports = () => config;
```

Create _main.mjs_, the entry point of the bundle, with the following contents:

_main.mjs_

```js
// GraalJS doesn't have built-in TextEncoder support yet. It's easy to import it from a polyfill in the meantime.
import 'fast-text-encoding';

// Re-export the "qrcode" module as a "QRCode" object in the exports of the bundle.
export * as QRCode from 'qrcode';
```

## 3. Use the JavaScript Library from Java

After reading the [qrcode](https://www.npmjs.com/package/qrcode) docs, you can write Java interfaces that match the [JavaScript types](https://www.npmjs.com/package/@types/qrcode) you want to use and methods you want to call on them.
GraalJS makes it easy to access JavaScript objects via these interfaces.
Java method names map directly to JavaScript function and method names.
You can choose interface names freely, but it's best to base them on the JavaScript types.

Define an interface that matches the JavaScript qrcode:

_src/main/java/com/example/QRCode.java_

```java
package com.example;

interface QRCode {
    Promise toString(String data);
}
```

Define an interface for handling JavaScript Promises:

_src/main/java/com/example/Promise.java_

```java
package com.example;

public interface Promise {
    Promise then(ValueConsumer onResolve);

    Promise then(ValueConsumer onResolve, ValueConsumer onReject);
}
```

Define a functional interface for consuming Polyglot values:

_src/main/java/com/example/ValueConsumer.java_

```java
package com.example;

import java.util.function.*;
import org.graalvm.polyglot.*;

@FunctionalInterface
public interface ValueConsumer extends Consumer<Value> {
    @Override
    void accept(Value value);
}
```

Use the following `Context` class and interfaces to create QR codes and convert them to a Unicode string representation or an image:

_src/main/java/com/example/App.java_

```java
package com.example;

import org.graalvm.polyglot.*;

public class App {
    public static void main(String[] args) throws Exception {
        try (Context context = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .option("engine.WarnInterpreterOnly", "false")
                    .option("js.esm-eval-returns-exports", "true")
                    .option("js.unhandled-rejections", "throw")
                    .option("js.text-encoding", "true")
                    .build()) {
            Source bundleSrc = Source.newBuilder("js", App.class.getResource("/bundle/bundle.mjs")).build(); // ①
            Value exports = context.eval(bundleSrc);
            QRCode qrCode = exports.getMember("QRCode").as(QRCode.class); // ②
            String input = args.length > 0 ? args[0] : "https://www.graalvm.org/javascript/";
            Promise resultPromise = qrCode.toString(input); // ③
            resultPromise.then( // ④
                (Value output) -> {
                    System.out.println("Successfully generated QR code for \"" + input + "\".");
                    System.out.println(output.asString());
                }
            );
        }
    }
}
```

❶ Load the bundle generated by `webpack` from a resource embedded in the JAR file.

❷ JavaScript objects are returned using a generic [Value](https://www.graalvm.org/truffle/javadoc/org/graalvm/polyglot/Value.html) type.
You can cast the exported `QRCode` object to the declared `QRCode` interface so that you can use Java typing and IDE completion features.

❸ `QRCode.toString` does not return the result directly but as a `Promise<string>` (alternatively, it can also be used with a callback).

❹ Invoke the `then` method of the `Promise` to eventually obtain the QRCode string and print it to `stdout`.

## 4. Build and Run the Application

If using Gradle, run the following commands to build and run your application:

```shell
./gradlew build
./gradlew run --args="https://www.graalvm.org/"
```

If using Maven, run the following commands to build and run your application:

```shell
mvn package
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="https://www.graalvm.org/"
```

The expected output should be similar to:

```
Successfully generated QR code for "https://www.graalvm.org/".

    █▀▀▀▀▀█  ▀▄ ▀▄█▄▀ █▀▀▀▀▀█
    █ ███ █ █▄ ▄ ▄▄▀▀ █ ███ █
    █ ▀▀▀ █ █  ▄▀▀▄▄█ █ ▀▀▀ █
    ▀▀▀▀▀▀▀ █ █▄▀ █▄▀ ▀▀▀▀▀▀▀
    █ ▀▀▀█▀▄ ▄█▀ █ ▀▄▄▀█▀▀▀▄
    ██▄  ▀▀▄ ▀▄▄█▀▀█▀█▀█▀▀ ▀█
    ██▀▀█▄▀█▄▄  ▄█▀▀▄█▀█▀▄▀█▀
    █ ▄█▄▀▀  ▀▀ ▄▀█▀ █▀██▀ ▀█
    ▀  ▀ ▀▀ ██▄ ▀▀█▀█▀▀▀█▄▀
    █▀▀▀▀▀█ ▄ ▄█▀▀  █ ▀ █▄▀▀█
    █ ███ █ ███▀█▀▀▀█▀█▀█▄█▄▄
    █ ▀▀▀ █ ▀▄▄▄ ▀█▄▄▄ ▄▄█▀ █
    ▀▀▀▀▀▀▀ ▀ ▀▀▀▀     ▀▀▀▀▀▀
```

## Conclusion

By following this guide, you've learned how to:

- Use GraalJS and the GraalVM Polyglot API to embed a JavaScript library in your Java application.
- Use Webpack to bundle an NPM package into a self-contained _.mjs_ file, including its dependencies and `polyfills` for Node.js core modules that may be required to run on GraalJS.
- Integrate the JavaScript build into your Java project using either Gradle or Maven.

Feel free to use this demo as inspiration or as a starting point for your own applications!
