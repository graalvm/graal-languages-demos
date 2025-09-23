plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // Apply GraalPy plugin to add Python packages as dependencies.
    id("org.graalvm.python") version "25.0.0"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

graalPy {
    packages = setOf(
        "openai==1.107.3",
    )
}

application {
    // Define the main class for the application.
    mainClass = "com.example.App"
}

val truffleJvmArgs = listOf("--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow")

tasks.named<JavaExec>("run") {
    jvmArgs = truffleJvmArgs
}

tasks.named<Test>("test") {
    jvmArgs = truffleJvmArgs
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
