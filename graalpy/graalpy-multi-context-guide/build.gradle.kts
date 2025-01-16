plugins {
    application
    id("org.graalvm.python") version "25.0.0"
}

graalPy {
    packages = setOf("pandas") // ①
    pythonHome { includes = setOf(); excludes = setOf(".*") } // ②
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("org.graalvm.python:python:25.0.0") // ①
    implementation("org.graalvm.python:python-embedding:25.0.0") // ③
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "graalpy-multi-context-guide"
java.sourceCompatibility = JavaVersion.VERSION_21

application {
    mainClass = "org.example.App"
}
