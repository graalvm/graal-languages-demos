plugins {
    application;
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation("org.graalvm.polyglot:python:25.0.0") // ①
    implementation("org.graalvm.polyglot:polyglot:25.0.0") // ③
    implementation("org.graalvm.tools:dap-tool:25.0.0") // ④
}

application {
    // Define the main class for the application.
    mainClass = "com.example.App"
}
