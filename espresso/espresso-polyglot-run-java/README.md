# Run Java code through the Polyglot API with Espresso

[`Espresso`](https://www.graalvm.org/latest/reference-manual/espresso/) is GraalVM's Java-on-Truffle implementation, which lets Java bytecode run as a guest language in polyglot applications.

This demo shows how a host Java application can start an Espresso guest context with the Polyglot API, load a guest Java class from the language bindings, and call both instance and static methods on it.

It intentionally uses compiled guest bytecode on the Espresso class path because Espresso does not support `Context.eval()` for Java source code.

## Preparation

Install [GraalVM 25.0.2](https://www.graalvm.org/downloads/) and set `JAVA_HOME` to that installation.

## Build the Demo

From this directory, run:

```bash
./mvnw package
```

## Run the Demo

You can run the demo through Maven with the same module-path based launch:

```bash
./mvnw package exec:exec
```

Pass a custom name to the guest Java code:

```bash
./mvnw package exec:exec -Ddemo.args="Duke"
```

Or execute the packaged app jar with the copied polyglot dependencies on the module path:

```bash
$JAVA_HOME/bin/java \
    --enable-native-access=org.graalvm.truffle \
    --sun-misc-unsafe-memory-access=allow \
    --module-path target/polyglot-libs \
    --add-modules=org.graalvm.polyglot \
    -cp target/espresso-polyglot-run-java-1.0-SNAPSHOT.jar \
    com.example.App
```

Pass a custom name to the guest Java code:

```bash
$JAVA_HOME/bin/java \
    --enable-native-access=org.graalvm.truffle \
    --sun-misc-unsafe-memory-access=allow \
    --module-path target/polyglot-libs \
    --add-modules=org.graalvm.polyglot \
    -cp target/espresso-polyglot-run-java-1.0-SNAPSHOT.jar \
    com.example.App Duke
```

The Maven command runs `$JAVA_HOME/bin/java` against `target/classes` with the GraalVM dependencies on the module path. The direct `java` command runs the packaged application JAR with the copied dependencies on the module path. In both cases, the host application resolves its own code source and passes that location to `java.Classpath`.

On JDK 24 and later, Truffle requires native access to avoid JDK warnings. For module-path launches, GraalVM recommends `--enable-native-access=org.graalvm.truffle`, and the JDK’s `--sun-misc-unsafe-memory-access=allow` option suppresses the `sun.misc.Unsafe` warning. For background and discussion, see [oracle/graal#12782](https://github.com/oracle/graal/issues/12782).

Note: this demo does not run *on* Espresso. It runs on a GraalVM JDK and embeds Espresso through the Polyglot API. The Espresso language runtime is pulled in by Maven as project dependencies. This is different from `espresso-continuations`, which requires launching the application on an Espresso-enabled Java runtime.

At the moment, this demo is expected to work on `linux-amd64`. Support for other platforms is a work in progress.
