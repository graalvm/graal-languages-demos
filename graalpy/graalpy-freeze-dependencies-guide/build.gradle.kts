plugins {
    application
    id("org.graalvm.python") version "24.1.1"
}

if ("true".equals(System.getProperty("no.transitive.dependencies"))) {
    graalPy {
        packages = setOf("vaderSentiment==3.3.2") // ①
    }
} else {
    // The default profile shows the end result: all our transitive
    // dependencies are explicitly pinned to a specific version.
    graalPy {
        packages = setOf(
            "vaderSentiment==3.3.2",
            "certifi==2024.8.30",
            "charset-normalizer==3.1.0",
            "idna==3.8",
            "requests==2.32.3",
            "urllib3==2.2.2"
        )
    }
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
  implementation("org.graalvm.python:python:24.1.1") // ①
  implementation("org.graalvm.python:python-embedding:24.1.1") // ③
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "javase"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass = "org.example.App"
    applicationDefaultJvmArgs = listOf("-Dgraalpy.resources=" + System.getProperty("graalpy.resources"))
}
