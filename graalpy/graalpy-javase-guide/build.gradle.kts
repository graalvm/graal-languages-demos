plugins {
    application
    id("org.graalvm.python") version "24.1.2"
}

graalPy {
    packages = setOf("qrcode==7.4.2") // ①
    pythonResourcesDirectory = file("${project.projectDir}/python-resources") // ②
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
  implementation("org.graalvm.python:python:24.1.2") // ①
  implementation("org.graalvm.python:python-embedding:24.1.2") // ③
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "javase"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass = "org.example.App"
    applicationDefaultJvmArgs = listOf("-Dgraalpy.resources=" + System.getProperty("graalpy.resources"))
}
