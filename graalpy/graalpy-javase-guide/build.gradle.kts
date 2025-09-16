plugins {
    application
    id("org.graalvm.python") version "25.0.0"
}

graalPy {
    packages = setOf("qrcode==7.4.2") // ①
    externalDirectory = file("${project.projectDir}/python-resources") // ②
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "javase"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    mainClass = "org.example.App"
    applicationDefaultJvmArgs = listOf("-Dgraalpy.resources=" + System.getProperty("graalpy.resources"))
}
