## GraalPy Apache Arrow Guide

## 1. Getting Started

This guide demonstrates how to use the Java Apache Arrow implementation library (JArrow) with GraalPy, achieving zero-copy memory when transferring data between Java and Python.

## 2. What you will need 
* Basic knowledge of JArrow
* Some time on your hands 
* A decent text editor or IDE
* A supported JDK[^1], preferably the latest [GraalVM JDK](https://graalvm.org/downloads/)

  [^1]: Oracle JDK 17 and OpenJDK 17 are supported with interpreter only.
  GraalVM JDK 21, Oracle JDK 21, OpenJDK 21 and newer with [JIT compilation](https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support).
  Note: GraalVM for JDK 17 is **not supported**.

## 3. Writing the application

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

or 

`build.gradle`
```
implementation "org.graalvm.python:python-community:24.2.0" // ①
implementation "org.graalvm.python:python-embedding:24.2.0" // ③
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

or

`build.gradle`
```
implementation "org.apache.arrow:arrow-vector:17.0.0" // ①
implementation "org.apache.arrow:arrow-memory-unsafe:17.0.0" // ②
```

❶ The `arrow-vector` dependency is used for managing in-memory columnar data structures.

❷ The `arrow-memory-unsafe` data structures defined in the `arrow-vector` will be backed by `sun.misc.Unsafe` library.
There is also another option `arrow-memory-netty`. You can read more about Apache Arrow memory management in [Apache Arrow documentation](https://arrow.apache.org/docs/java/memory.html)


## 3.3 Adding packages - GraalPy build plugin configuration

`pom.xml`
```xml
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
      </configuration>
      <goals>
        <goal>process-graalpy-resources</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

or 

`build.gradle`
```
plugins {
    id 'org.graalvm.python' version '24.2.0'
    // ...
```

`build.gradle`
```
graalPy {
    community = true
    packages = [ // ①
                 'pandas', // ②
                 'pyarrow' // ③
    ]
}
```

❶ The `packages` section lists all Python packages optionally with [requirement specifiers](https://pip.pypa.io/en/stable/reference/requirement-specifiers/).

❷ Python packages and their versions can be specified as if used with pip. You can either install the latest version or you can specify the version e.g.`pandas==2.2.2`.

❸ The `pandas` package does not declare `pyarrow` as a transitive dependency so it has to done so manually at this place.



## 3.4 Creating a Python context
`Main.java`
```java
public static Context initContext() throws IOException {
  var resourcesDir = Path.of(System.getProperty("user.home"), ".cache", "graalpy-apache-arrow-guide.resources"); // ①
  var fs = VirtualFileSystem.create();
  GraalPyResources.extractVirtualFileSystemResources(fs, resourcesDir); // ②
  return GraalPyResources.contextBuilder(resourcesDir)
          .option("python.PythonHome", "")
          .option("python.WarnExperimentalFeatures", "false")
          .allowHostAccess(HostAccess.ALL)
          .allowHostClassLookup(_ -> true)
          .allowNativeAccess(true)
          .build(); // ③
}
```

❶ Specify directory where resources will be copied.

❷ Copy the resources from Virtual File System to the directory specified. This step is needed because PyArrow ...? 

❸ Create the context with the given configuration. 


## 3.5 Initialize Python module


We'll create a Python module in this section and bind it to a Java interface, allowing the Java interface to call Python methods defined in the module.

All Python source code should be placed in `src/main/resources/org.graalvm.python.vfs/src`

Let's create a `data_analysis.py` file to calculate the mean and median for the Float8Vector using Pandas:
```python
import pandas as pd
from polyglot.arrow import Float8Vector, enable_java_integration

enable_java_integration() # ①

def calculateMean(valueVector: Float8Vector) -> float:
  series = pd.Series(valueVector, dtype="float64[pyarrow]") # ②
  return series.mean()


def calculateMedian(valueVector: Float8Vector) -> float:
  series = pd.Series(valueVector, dtype="float64[pyarrow]")
  return series.median()

```

❶ You need to call this method to enable the zero copy integration.

❷ In pandas you need to specify that the series should be backed by pyarrow, therefore adding `[pyarrow]` to the dtype. 



### 3.5.1 Binding Java interface with Python code

Define a Java interface with the methods we want to bind. Remember, the method names must match those in the Python code.

`DataAnalysisPyModule.java`
```java

public interface DataAnalysisPyModule {
    double calculateMean(Float8Vector valueVector);
    double calculateMedian(Float8Vector valueVector);
}
```

Bind the Java interface to the Python module.

`Main.java`

```java
    private static DataAnalysisPyModule dataAnalysisPyModule;

    public static void initDataAnalysisPyModule(Context context) {
        Value value = context.eval("python", "import data_analysis; data_analysis");
        dataAnalysisPyModule = value.as(DataAnalysisPyModule.class);
    }
```

## 3.6 Running the Application

Everything is set, and we can now try it out. In this example, we'll download test reports for GraalPy and GraalJS, and then calculate the mean and median values. We handle the data download on the Java side to take advantage of true parallelism, which isn't possible in Python due to the Global Interpreter Lock (GIL).

To download the data, we've prepared a helper method. This method also populates a JArrow float vector, which will be passed directly to the Python code.

`DownloadUtils.java`
```java
    public static void downloadAndStore(String url, int columnIndex, Float8Vector vectorToStore) {
        try {
            var httpConnection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()))) {
                    String line;
                    int index = 0;
                    while ((line = reader.readLine()) != null) {
                        var values = line.split(",");
                        double value = Double.parseDouble(values[columnIndex]);
                        vectorToStore.setSafe(index, value);
                        index++;
                    }
                    vectorToStore.setValueCount(index);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
```

Finally, use the setup in your main method:

`Main.java`
```java
    private static final String PYTHON_URL = "https://www.graalvm.org/compatibility/module_results/python-module-testing-v241.csv";
    private static final String JAVASCRIPT_URL = "https://www.graalvm.org/compatibility/module_results/js-module-testing.csv";
    private static final Integer PASSING_RATE_COLUMN_INDEX = 3;    


    public static void main(String[] args) throws IOException, InterruptedException {
        try (Context context = initContext();
             BufferAllocator allocator = new RootAllocator();
             Float8Vector pyVector = new Float8Vector("python", allocator);
             Float8Vector jsVector = new Float8Vector("javascript", allocator)
        ) {
            initDataAnalysisPyModule(context);
            Thread pyThread = new Thread(() -> DownloadUtils.downloadAndStore(PYTHON_URL, PASSING_RATE_COLUMN_INDEX, pyVector));
            Thread jsThread = new Thread(() -> DownloadUtils.downloadAndStore(JAVASCRIPT_URL, PASSING_RATE_COLUMN_INDEX, jsVector));
            pyThread.start();
            jsThread.start();
            pyThread.join();
            jsThread.join();

            System.out.println("Python mean: " + dataAnalysisPyModule.calculateMean(pyVector));
            System.out.println("Python median: " + dataAnalysisPyModule.calculateMedian(pyVector));
            System.out.println("JS mean: " + dataAnalysisPyModule.calculateMean(jsVector));
            System.out.println("JS median: " + dataAnalysisPyModule.calculateMedian(jsVector));
        }
    }
```

To compile the application: 
```bash
./mvnw package
```

or

```bash
./gradlew build
```

To run the application using Maven, first define `exec` plugin:

`pom.xml`
```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>1.2.1</version>
  <executions>
    <execution>
      <goals>
        <goal>java</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <mainClass>com.example.Main</mainClass>
  </configuration>
</plugin>
```
Run the application using:
```bash
./mvnw exec:java -Dexec.mainClass="com.example.Main"
```

or

```bash
./gradlew run
```