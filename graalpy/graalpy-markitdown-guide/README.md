## MarkitDown in Java Application 

## 1. Getting Started

In this guide, we will demonstrate how to use [MarkitDown](https://github.com/microsoft/markitdown) within a Java application through GraalPy.
## 2. What you will need

To complete this guide, you will need the following:

* Some time on your hands
* A decent text editor or IDE
* A supported JDK[^1], preferably the latest [GraalVM JDK](https://graalvm.org/downloads/)

  [^1]: Oracle JDK 17 and OpenJDK 17 are supported with interpreter only.
  GraalVM JDK 21, Oracle JDK 21, OpenJDK 21 and newer with [JIT compilation](https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support).
  Note: GraalVM for JDK 17 is **not supported**.


## 3. Writing the application

We will use Maven to run a simple Java application that integrates `MarkitDown` using `GraalPy`.

### 3.1 Dependency configuration


Add the required dependencies for `GraalPy` in the dependency section of the POM build script.


`pom.xml`

```xml
  <dependency>
    <groupId>org.graalvm.python</groupId>
    <artifactId>python-community</artifactId> <!-- ① -->
    <version>${graalpy.version}</version>
    <type>pom</type> <!-- ② -->
  </dependency>
  <dependency>
    <groupId>org.graalvm.python</groupId>
    <artifactId>python-embedding</artifactId> <!-- ③ -->
    <version>${graalpy.version}</version>
  </dependency>

```



❶ The `python-community` dependency is a meta-package that transitively depends on all resources and libraries to run GraalPy.

❷ Note that the `python-community` package is not a JAR - it is simply a pom that declares more dependencies.

❸ The `python-embedding` dependency provides the APIs to manage and use GraalPy from Java.



### 3.2 Adding packages - GraalPy build plugin configuration
```xml
<plugin>
    <groupId>org.graalvm.python</groupId>
    <artifactId>graalpy-maven-plugin</artifactId>
    <version>${graalpy.version}</version>
    <executions>
        <execution>
            <configuration>
                <packages> <!-- ① -->
                    <!-- Select Python packages to install via pip. -->
                   <package>markitdown[all]</package> <!-- ② -->
                    <!-- Add any other packages here -->
                </packages>
            </configuration>
            <goals>
                <goal>process-graalpy-resources</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```


❶ The packages section lists all Python packages optionally with requirement specifiers.

❷ Python packages are specified as if used with pip (latest version or pinned version)




### 3.4 Creating a Python context


GraalPy makes setting up a context to load Python packages from Java straightforward.

`App.java`

```java
 try (Context context = GraalPyResources.contextBuilder()
        .allowAllAccess(true) // ①
        .option("python.WarnExperimentalFeatures", "false") // ②
        .build()) { // ③

```

❶ Allow environment access.


❷ Set GraalPy option to not log a warning every time a native extension is loaded.


❸ Create the context with the given configuration.


### 3.5 Initialize Python module

We'll create a Python module in this section and bind it to a Java interface, allowing the Java interface to call Python methods defined in the module.

All Python source code should be placed in `src/main/resources/org.graalvm.python.vfs/src`

`convert_file.py`

```python
from markitdown import MarkItDown

def convert(file: str) -> str: # ①
  md = MarkItDown(enable_plugins=False)
    result = md.convert(file)
    return result.text_content
```


❶ The `convert` function converts any supported file format to plain text.


### 3.5.1 Binding Java interface with Python code


Define a Java interface with method signatures matching the Python functions.

`ConvertFile.java`

```java

package org.example;

public interface ConvertFiles {
  String convert(String file);
}

```


### 3.5.2 Binding the Java interface to the Python module

Once the Python function is defined, we can bind it to the Java interface so that calling the Python function feels like a normal Java method invocation:

```java
Value value = context.eval("python", "import convert_file; convert_file");
ConvertFile convertFile = value.as(ConvertFile.class);
String text = convertFile.convert("src/main/resources/test.pdf");
System.out.println(text);

```

This allows Java code to call the `convert()` function from the Python module seamlessly.

### 4 Running the Application

If you followed along with the example, you can now compile and run your application from the commandline:

```shell
mvn compile
mvn exec:java
```

