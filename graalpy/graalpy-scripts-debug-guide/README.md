# Using and Debugging Python Scripts in Java Applications using VSCode

Simple, unpackaged Python scripts can be run and shipped with Java applications.
The [GraalVM Polyglot APIs](https://www.graalvm.org/latest/reference-manual/embed-languages/) make it easy to run scripts that are simply included in the Java resources.

## 1. Getting Started

In this guide, we will add a small Python script to calculate the similarity of two files to a JavaFX application:
![Screenshot of the app](screenshot.png)

## 2. What you will need

To complete this guide, you will need the following:

 * Some time on your hands
 * A decent text editor or IDE
 * A supported JDK[^1], preferably the latest [GraalVM JDK](https://graalvm.org/downloads/)

 [^1]: Oracle JDK 17 and OpenJDK 17 are supported with interpreter only for GraalPy, but JavaFX requires JDK 21 or newer.
 GraalVM JDK 21, Oracle JDK 21, OpenJDK 21 and offer GraalPy [JIT compilation](https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support).
 Note: GraalVM for JDK 17 is **not supported** for GraalPy.

## 3. Solution

We encourage you to check out the [completed example](./) and follow with this guide step by step.

## 4. Writing the application

You can use either [Maven](https://openjfx.io/openjfx-docs/#maven) or [Gradle](https://openjfx.io/openjfx-docs/#gradle) to run the JavaFX example application.
We will demonstrate on both build systems.

## 4.1 Dependency configuration

We have added the required dependencies for GraalPy in the `<dependencies>` section of the POM or to the `dependencies` block in the `build.gradle.kts` file.

`pom.xml`
```xml
<dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>python</artifactId> <!-- ① -->
    <version>24.1.1</version>
    <type>pom</type> <!-- ② -->
</dependency>
<dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>polyglot</artifactId> <!-- ③ -->
    <version>24.1.1</version>
</dependency>
<dependency>
    <groupId>org.graalvm.tools</groupId>
    <artifactId>dap-tool</artifactId> <!-- ④ -->
    <version>24.1.1</version>
</dependency>
```

`build.gradle.kts`
```kotlin
implementation("org.graalvm.polyglot:python:24.1.1") // ①
implementation("org.graalvm.polyglot:polyglot:24.1.1") // ③
implementation("org.graalvm.tools:dap-tool:24.1.1") // ④
```

❶ The `python` dependency is a meta-package that transitively depends on all resources and libraries to run GraalPy.

❷ Note that the `python` package is not a JAR - it is simply a `pom` that declares more dependencies.

❸ The `polyglot` dependency provides the APIs to manage and use GraalPy from Java.

❹ The `dap` dependency provides a remote debugger for GraalPy that we can use when Python code is embedded in a Java application.

## 4.2 Adding a Python script

We can just include simple Python scripts in our resources source folder.
In this example, the script contains a function that uses the Python standard library to compute the similarity between two files.

`src/main/resources/compare_files.py`
```python
import polyglot  # pyright: ignore

from difflib import SequenceMatcher
from os import PathLike


@polyglot.export_value # ①
def compare_files(a: PathLike, b: PathLike) -> float:
    with open(a) as file_1, open(b) as file_2: 
        file1_data = file_1.read() 
        file2_data = file_2.read() 
        similarity_ratio = SequenceMatcher(None, file1_data, file2_data).ratio()
        return similarity_ratio
```

❶ The only GraalPy-specific code here is this `polyglot.export_value` annotation, which makes the function accessible by name to the Java world.

## 4.2.1 Working with GraalPy in VSCode

You can use [pyenv](https://github.com/pyenv/pyenv) or [pyenv-win](https://github.com/pyenv-win/pyenv-win) with the [Python extensions](https://marketplace.visualstudio.com/items?itemName=ms-python.python) in VSCode to setup and use GraalPy during development.
You can than edit and debug your Python files using the standard Python tooling.

![Gif animation of installing GraalPy with pyenv](./graalpy-vscode-pyenv.gif)
![Gif animation of using GraalPy in VSCode](./graalpy-vscode-select.gif)
![Gif animation of debugging with GraalPy in VSCode](./graalpy-vscode-debug.gif)

## 4.3 Creating a Python context

GraalVM provides Polyglot APIs to make starting a Python context easy.
We create the Python context in the JavaFX `start` method.
We also override the `stop` method to close the context and free any associated resources.

`App.java`
```java
public class App extends Application {
    private Context context;

    @Override
    public void stop() throws Exception {
        context.close();
        super.stop();
    }

    @Override
    public void start(Stage stage) {
        context = Context.newBuilder("python")
            .allowIO(IOAccess.newBuilder() // ①
                            .fileSystem(FileSystem.newReadOnlyFileSystem(FileSystem.newDefaultFileSystem()))
                            .build())
            .allowPolyglotAccess(PolyglotAccess.newBuilder() // ②
                            .allowBindingsAccess("python")
                            .build())
            // These are all the options we need to run the app
```

❶ By default, GraalPy will be sandboxed completely, but our script wants to access files.
Read-only access is enough for this case, so we grant no more.

❷ Our script exposes the `compare_files` function by name to the Java world.
We explicitly allow this as well.

## 4.3 Calling the Python script from Java

`App.java`
```java
try {
    context.eval(Source.newBuilder("python", App.class.getResource("/compare_files.py")).build());  // ①
} catch (IOException e) {
    throw new RuntimeException(e);
}
final Value compareFiles = context.getBindings("python").getMember("compare_files"); // ②

target.setOnDragDropped((event) -> {
    var success = false;
    List<File> files;
    if ((files = event.getDragboard().getFiles()) != null && files.size() == 2) {
        try {
            File file0 = files.get(0), file1 = files.get(1);
            var result = compareFiles.execute(file0.getAbsolutePath(), file1.getAbsolutePath()).asDouble(); // ③
            target.setText(String.format("%s = %f x %s", file0.getName(), result, file1.getName()));
            success = true;
        } catch (RuntimeException e) {
            target.setText(e.getMessage());
        }
    }
    resetTargetColor(target);
    event.setDropCompleted(success);
    event.consume();
});
```

❶ We can pass a resource URL to the GraalVM Polyglot [`Source`](https://docs.oracle.com/en/graalvm/enterprise/20/sdk/org/graalvm/polyglot/Source.html) API.
The content is read by the `Source` object, GraalPy and the Python code do not gain access to Java resources this way.

❷ Python objects are returned using a generic [`Value`](https://docs.oracle.com/en/graalvm/enterprise/20/sdk/org/graalvm/polyglot/Value.html) type.

❸ As a Python function, `compare_files` can be executed.
GraalPy accepts Java objects and tries to match them to the appropriate Python types.
Return values are again represented as `Value`.
In this case we know the result will be a Python `float`, which can be converted to a Java `double`.

## 5. Running the application

If you followed along with the example, you can now compile and run your application from the commandline:

With Maven:

```shell
./mvnw compile
./mvnw javafx:run
```

With Gradle:

```shell
./gradlew assemble
./gradlwe run
```

## 5.1 Debugging embedded Python code

Your Python code may behave differently when run in a Java embedding.
This can have many reasons, from different types passed in from Java, permissions of the GraalVM Polyglot sandbox, to Python libraries assuming OS-specific process properties that Java applications do not expose.

To debug Python scripts, we recommend you use VSCode.
Make sure you have installed the [Python extensions](https://marketplace.visualstudio.com/items?itemName=ms-python.python).
Where we build the Python context, we can add the following options to enable remote debugging:

`App.java`
```java
.option("dap", "localhost:4711")
.option("dap.Suspend", "false")
```

This instructs the runtime to accept [DAP]() connections on port 4711 and continue execution.
We add a debug configuration to VSCode to match:

`.vscode/launch.json`
```json
{
    "configurations": [{
        "name": "GraalPy: Attach embedded",
        "type": "debugpy",
        "request": "attach",
        "connect": { "host": "localhost", "port": 4711 },
    }]
}
```

When we run the application now, we will see the following output:

```
[Graal DAP] Starting server and listening on localhost/127.0.0.1:4711
```

We can connect using VSCode or any other DAP client.
The loaded sources can be opened to view the Python code as loaded from the Java resources.
We can set breakpoints and inspect runtime state as we would expect.

![Gif animation debugging GraalPy in Java in VSCode](./graalpy-vscode-dap-debug.gif)

## 6. Next steps

- Use GraalPy with popular Java frameworks, such as [Spring Boot](../graalpy-spring-boot-guide/README.md) or [Micronaut](../graalpy-micronaut-guide/README.md)
- [Migrate from Jython](../graalpy-jython-guide/README.md) to GraalPy
- Learn more about the Polyglot API for [embedding languages](https://www.graalvm.org/latest/reference-manual/embed-languages/)
- Explore in depth with GraalPy [reference manual](https://www.graalvm.org/latest/reference-manual/python/)