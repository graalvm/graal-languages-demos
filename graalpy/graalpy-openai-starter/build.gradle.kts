plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    // Apply GraalPy plugin to add Python packages as dependencies.
    id("org.graalvm.python") version "24.2.2"
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
        "annotated-types==0.7.0",
        "anyio==4.6.0",
        "certifi==2024.8.30",
        "distro==1.9.0",
        "h11==0.14.0",
        "hpy==0.9.0",
        "httpcore==1.0.5",
        "httpx==0.27.2",
        "idna==3.10",
        "jiter==0.5.0", // uses a native extension
        "openai==1.47.1",
        "pydantic==2.4.2",
        "pydantic_core==2.10.1", // uses a native extension
        "sniffio==1.3.1",
        "tqdm==4.66.5",
        "typing_extensions==4.12.2"
    )
}

application {
    // Define the main class for the application.
    mainClass = "com.example.App"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
