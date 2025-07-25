# Using Python Packages in a Java SE Application

Python libraries can be used in and shipped with plain Java applications.
The [GraalPy Maven artifacts](https://central.sonatype.com/artifact/org.graalvm.polyglot/python) and [GraalVM Polyglot APIs](https://www.graalvm.org/latest/reference-manual/embed-languages/) allow flexible integration with different project setups.

Using Python packages in Java projects often requires a bit more setup, due to the nature of the Python packaging ecosystem.
GraalPy provides a [python-embedding](https://central.sonatype.com/artifact/org.graalvm.python/python-embedding) package that simplifies the required setup to ship Python packages as Java resources or in separate folders.
The important entry points to do so are the [VirtualFileSystem](https://github.com/oracle/graalpython/blob/master/docs/user/Embedding-Build-Tools.md#virtual-filesystem) and the [GraalPyResources](https://github.com/oracle/graalpython/blob/master/docs/user/Embedding-Build-Tools.md#deployment) classes.

## 1. Getting Started

In this guide, we will add a small Python library to [generate QR codes](https://pypi.org/project/qrcode) to a Java GUI application:
![Screenshot of the app](screenshot.png)

## 2. What you will need

To complete this guide, you will need the following:

 * Some time on your hands
 * A decent text editor or IDE
 * A supported JDK[^1], preferably the latest [GraalVM JDK](https://graalvm.org/downloads/)

 [^1]: Oracle JDK 17 and OpenJDK 17 are supported with interpreter only.
 GraalVM JDK 21, Oracle JDK 21, OpenJDK 21 and newer with [JIT compilation](https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support).
 Note: GraalVM for JDK 17 is **not supported**.

## 3. Solution

We recommend that you follow the instructions in the next sections and create the application step by step.
However, you can go right to the [completed example](./).

## 4. Writing the application

You can start with any Maven or Gradle application that runs on JDK 17 or newer.
We will demonstrate on both build systems.
A default Maven application [generated](https://maven.apache.org/archetypes/maven-archetype-quickstart/) from an archetype.

```shell
mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes \
  -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 \
  -DgroupId=example -DartifactId=javase -Dpackage=org.example \
  -Dversion=1.0-SNAPSHOT -DinteractiveMode=false
```

And a default Gradle Java application [generated](https://docs.gradle.org/current/samples/sample_building_java_applications.html#run_the_init_task) with the init task.

```shell
gradle init --type java-application --dsl kotlin --test-framework junit-jupiter \
    --package org.example --project-name javase --java-version 17 \
    --no-split-project --no-incubating
```

## 4.1 Dependency configuration

Add the required dependencies for GraalPy in the `<dependencies>` section of the POM file for Maven.
For Gradle, the GraalPy Gradle plugin that we will add in the next section will inject these
dependencies automatically.

`pom.xml`
```xml
<dependencies>
  <dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>python</artifactId> <!-- ① -->
    <version>24.2.1</version>
    <type>pom</type> <!-- ② -->
  </dependency>

  <dependency>
    <groupId>org.graalvm.python</groupId>
    <artifactId>python-embedding</artifactId> <!-- ③ -->
      <version>24.2.1</version>
  </dependency>
</dependencies>
```

❶ The `python` dependency is a meta-package that transitively depends on all resources and libraries to run GraalPy.

❷ Note that the `python` package is not a JAR - it is simply a `pom` that declares more dependencies.

❸ The `python-embedding` dependency provides the APIs to manage and use GraalPy from Java.

## 4.2 Adding packages

Most Python packages are hosted on [PyPI](https://pypi.org) and can be installed via the `pip` tool.
The Python ecosystem has conventions about the filesystem layout of installed packages that need to be kept in mind when embedding into Java.
You can use the GraalPy plugins for Maven or Gradle to manage Python packages for you.

`pom.xml`
```xml
 <build>
   <plugins>
     <plugin>
       <groupId>org.graalvm.python</groupId>
       <artifactId>graalpy-maven-plugin</artifactId>
       <version>24.2.1</version>
       <configuration>
         <packages> <!-- ① -->
           <package>qrcode==7.4.2</package>
         </packages>
         <externalDirectory> <!-- ② -->
           ${project.basedir}/python-resources
         </externalDirectory>
       </configuration>
       <executions>
         <execution>
           <goals>
             <goal>process-graalpy-resources</goal>
           </goals>
         </execution>
       </executions>
     </plugin>
   </plugins>
 </build>
```

`build.gradle.kts`
```kotlin
plugins {
    application
    id("org.graalvm.python") version "24.2.1"
}

graalPy {
    packages = setOf("qrcode==7.4.2") // ①
    externalDirectory = file("${project.projectDir}/python-resources") // ②
}
```

❶ The `packages` section lists all Python packages optionally with [requirement specifiers](https://pip.pypa.io/en/stable/reference/requirement-specifiers/).
In this case, we install the `qrcode` package and pin it to version `7.4.2`.

<a name="external-or-embedded-python-code-pom"></a>
❷ We can specify where the plugin should place Python files for packages that the application will use.
Omit this section if you want to include the Python packages into the Java resources (and, for example, ship them in the Jar).
[Later in the Java code](#external-or-embedded-python-code-java) we can configure the GraalPy runtime to load the package from the filesystem or from resources.

**Note** if you are using older version 24.2.0: due to a bug in the `org.graalvm.python` plugin for **Gradle** you need to include a resource.
A simple workaround is to add a `src/main/resources/META-INF/MANIFEST.MF`:
```
Manifest-Version: 1.0
```

## 4.3 Creating a Python context

GraalPy provides APIs to make setting up a context to load Python packages from Java as easy as possible.

`GraalPy.java`
```java
package org.example;

import java.nio.file.Path;

import org.graalvm.polyglot.Context;
import org.graalvm.python.embedding.*;

public class GraalPy {
    static VirtualFileSystem vfs;

    public static Context createPythonContext(String pythonResourcesDirectory) { // ①
        return GraalPyResources.contextBuilder(Path.of(pythonResourcesDirectory)).build();
    }

    public static Context createPythonContextFromResources() {
        if (vfs == null) { // ②
            vfs = VirtualFileSystem.newBuilder().allowHostIO(VirtualFileSystem.HostIO.READ).build();
        }
        return GraalPyResources.contextBuilder(vfs).build();
    }
}
```

❶ [If we set the `pythonResourcesDirectory` property](#external-or-embedded-python-code-pom) in our build config, we use this factory method to tell GraalPy where that folder is at runtime.

❷ [If we do not set the `externalDirectory` property](#external-or-embedded-python-code-pom), the GraalPy Maven or Gradle plugin will place the packages inside the Java resources.
Because Python libraries assume they are running from a filesystem, not a resource location, GraalPy provides the `VirtualFileSystem`, and API to make Java resource locations available to Python code as if it were in the real filesystem.
VirtualFileSystem instances can be configured to allow different levels of through-access to the underlying host filesystem.
In this demo we use the same VirtualFileSystem instance in multiple Python contexts.

## 4.3 Using a Python library from Java

After reading the [qrcode](https://pypi.org/project/qrcode/) docs, we can write Java interfaces that match the Python types we want to use and methods we want to call on them.
GraalPy makes it easy to access Python objects via these interfaces.
Java method names are mapped directly to Python method names.
Return values are mapped according to a set of [generic rules](https://www.graalvm.org/latest/reference-manual/python/Modern-Python-on-JVM/#java-to-python-types-automatic-conversion).
The names of the interfaces can be chosen freely, but it makes sense to base them on the Python types, as we do below.

`QRCode.java`
```java
package org.example;

interface QRCode {
    PyPNGImage make(String data);

    interface PyPNGImage {
        void save(IO.BytesIO bio);
    }
}
```

`IO.java`
```java
package org.example;

import org.graalvm.polyglot.io.ByteSequence;

interface IO {
    BytesIO BytesIO();

    interface BytesIO {
        ByteSequence getvalue();
    }
}
```

Using these interfaces and the `GraalPy` class, we can now create QR-codes and show them in, for example, a [JLabel](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/JLabel.html).

`App.java`
```java
package org.example;

import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class App {
    public static void main(String[] args) throws IOException {
        String path = System.getProperty("graalpy.resources");
        if (path == null || path.isBlank() || path.equals("null")) {
            System.err.println("Please provide 'graalpy.resources' system property.");
            System.exit(1);
        }
        try (var context = GraalPy.createPythonContext(path)) { // ①
            QRCode qrCode = context.eval("python", "import qrcode; qrcode").as(QRCode.class); // ②
            IO io = context.eval("python", "import io; io").as(IO.class);

            IO.BytesIO bytesIO = io.BytesIO(); // ③
            qrCode.make("Hello from GraalPy on JDK " + System.getProperty("java.version")).save(bytesIO);

            var qrImage = ImageIO.read(new ByteArrayInputStream(bytesIO.getvalue().toByteArray())); // ④
            JFrame frame = new JFrame("QR Code");
            frame.getContentPane().add(new JLabel(new ImageIcon(qrImage)));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(400, 400);
            frame.setVisible(true);
        }
    }
}
```

❶ If we do not want to ship the directory with the Python package separately and pass the location at runtime, we can [embed them](#external-or-embedded-python-code-pom) and use the virtual filesystem [constructor](#external-or-embedded-python-code-java).

❷ Python objects are returned using a generic [Value](https://docs.oracle.com/en/graalvm/enterprise/20/sdk/org/graalvm/polyglot/Value.html) type.
We cast the `io` and `qrcode` packages to our declared interfaces so we can use Java typing and IDE completion features.

❸ Method calls on our interfaces are transparently forwarded to the Python objects, arguments and return values are coerced automatically.

❹ Python code returns the generated PNG as an array of unsigned bytes, which we can process on the Java side.

## 5. Running the application

If you followed along with the example, you can now compile and run your application from the commandline:

With Maven:

```shell
./mvnw compile
./mvnw exec:java -Dexec.mainClass=org.example.App -Dgraalpy.resources=./python-resources
```

With Gradle:

Update the build script to pass the necessary Java property to the application:

`build.gradle.kts`
```
application {
    mainClass = "org.example.App"
    applicationDefaultJvmArgs = listOf("-Dgraalpy.resources=" + System.getProperty("graalpy.resources"))
}
```

Run from command line:

```shell
./gradlew assemble
./gradlew -Dgraalpy.resources=./python-resources run
```

## 6. Next steps

- Use GraalPy with popular Java frameworks, such as [Spring Boot](../graalpy-spring-boot-guide/README.md) or [Micronaut](../graalpy-micronaut-guide/README.md)
- Install and use Python packages that rely on [native code](../graalpy-native-extensions-guide/README.md), e.g. for data science and machine learning
- Follow along how you can manually [install Python packages and files](../graalpy-custom-venv-guide/README.md) if the Maven plugin gives not enough control
- [Freeze](../graalpy-freeze-dependencies-guide/README.md) transitive Python dependencies for reproducible builds
- [Migrate from Jython](../graalpy-jython-guide/README.md) to GraalPy


- Learn more about the GraalPy [Maven plugin](https://www.graalvm.org/latest/reference-manual/python/Embedding-Build-Tools/)
- Learn more about the Polyglot API for [embedding languages](https://www.graalvm.org/latest/reference-manual/embed-languages/)
- Explore in depth with GraalPy [reference manual](https://www.graalvm.org/latest/reference-manual/python/)
