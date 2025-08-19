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
    implementation("org.graalvm.polyglot:python:24.2.2") // ①
    implementation("org.graalvm.polyglot:polyglot:24.2.2") // ③
    implementation("org.graalvm.tools:dap-tool:24.2.2") // ④
}

application {
    // Define the main class for the application.
    mainClass = "com.example.App"
}
