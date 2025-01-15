## GraalPy Apache Arrow Guide

## 1. Getting Started

In this guide, we demonstrate how to use Java Apache Arrow implementation library (JArrow) together with GraalPy while achieving zero copy memory when data are moved from Java to Python. Keep in mind that the current API is experimental and can change in the future. 

## 2. What you will need 
* Basic knowledge of JArrow
* Some time on your hands 
* A decent text editor or IDE
* A supported JDK[^1], preferably the latest [GraalVM JDK](https://graalvm.org/downloads/)

  [^1]: Oracle JDK 17 and OpenJDK 17 are supported with interpreter only.
  GraalVM JDK 21, Oracle JDK 21, OpenJDK 21 and newer with [JIT compilation](https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support).
  Note: GraalVM for JDK 17 is **not supported**.

## 3. Dependency configuration

Add the required dependencies for GraalPy and JArrow in the dependency section of the POM.

### 3.1 GraalPy dependencies
`pom.xml`
```xml
<dependency>
    <groupId>org.graalvm.python</groupId>
    <artifactId>python-community</artifactId> <!-- ① -->
    <version>${python.version}</version>
    <type>pom</type> <!-- ② -->
</dependency>
<dependency>
    <groupId>org.graalvm.python</groupId>
    <artifactId>python-embedding</artifactId> <!-- ③ -->
    <version>${python.version}</version>
</dependency>
```

❶ The `python-community` dependency is a meta-package that transitively depends on all resources and libraries to run GraalPy.

❷ Note that the `python-community` package is not a JAR - it is simply a `pom` that declares more dependencies.

❸ The `python-embedding` dependency provides the APIs to manage and use GraalPy from Java.


### 3.2 JArrow dependencies
`pom.xml`
```xml
<dependency>
    <groupId>org.apache.arrow</groupId>
    <artifactId>arrow-vector</artifactId> <!-- ① -->
    <version>${arrow.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.arrow</groupId>
    <artifactId>arrow-memory-unsafe</artifactId> <!-- ② -->
    <version>${arrow.version}</version>
</dependency>
```

❶ The `arrow-vector` dependency is used for managing in-memory columnar data structures.

❷ The `arrow-memory-unsafe` data structures defined in the `arrow-vector` will be backed by `sun.misc.Unsafe` library.
There is also another option `arrow-memory-netty`. You can read more about Apache Arrow memory management in [Apache Arrow documentation](https://arrow.apache.org/docs/java/memory.html)


## 4. Adding packages - GraalPy build plugin configuration

`pom.xml`
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.python</groupId>
                <artifactId>graalpy-maven-plugin</artifactId>
                <version>${python.version}</version>
                <executions>
                    <execution>
                        <configuration>
                            <packages> <!-- ① -->
                                <package>pandas</package> <!-- ② -->
                                <package>pyarrow</package> <!-- ③ -->
                            </packages>
                            <pythonHome>
                                <includes></includes>
                                <excludes>.*</excludes>
                            </pythonHome>
                        </configuration>
                        <goals>
                            <goal>process-graalpy-resources</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

❶ The `packages` section lists all Python packages optionally with [requirement specifiers](https://pip.pypa.io/en/stable/reference/requirement-specifiers/).

❷ Python packages and their versions can be specified as if used with pip. You can either install the latest version or you can specify the version e.g.`pandas==2.2.2`.

❸ The `pandas` package does not declare `pyarrow` as a transitive dependency so it has to done so manually at this place.



TBD