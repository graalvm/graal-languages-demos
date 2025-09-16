# Suspend and resume Java applications using Espresso's Continuation API

This demo persists its state by saving it to a file.
Each time it runs, the program increments a counter, displays the updated value, and then exits.

## Preparation

Install [Espresso 25.0.0](https://www.graalvm.org/latest/reference-manual/espresso/) and set the value of `JAVA_HOME` accordingly.

## Run the Application

To build the demo, run:

```bash
./mvnw package
```

To execute the main method, run:

```bash
$JAVA_HOME/bin/java --experimental-options --java.Continuum=true \
    -jar target/demo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

To use Kryo as serializer, use the `-s` application argument:

```bash
$JAVA_HOME/bin/java --experimental-options --java.Continuum=true \
    -jar target/demo-1.0-SNAPSHOT-jar-with-dependencies.jar -s kryo
```

Note: the default Java and Kryo serializers are not compatible with each other.
When switching between serializers, delete the _state.serial.bin_ file.
