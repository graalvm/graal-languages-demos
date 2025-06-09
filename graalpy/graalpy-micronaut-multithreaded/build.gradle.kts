plugins {
    id("org.graalvm.python") version "24.2.1"
    // ...
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.2"
    id("io.micronaut.aot") version "4.4.2"
}

graalPy {
    packages = setOf( // ①
        "numpy==1.26.4", // ②
        "--no-binary=numpy", // ③
        mapOf( // ④
            "linux" to "patchelf==0.17.2.2",
            "windows" to "delvewheel==1.10.0",
            "mac" to "delocate==0.13.0"
        )[System.getProperty("os.name").split(" ")[0].toLowerCase()]
    )
}

version = "0.1"
group = "graalpy.micronaut.multithreaded"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.micronaut.views:micronaut-views-thymeleaf")
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    testImplementation("io.micronaut:micronaut-http-client")
}

application {
    mainClass = "graalpy.micronaut.multithreaded.Application"
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("graalpy.micronaut.multithreaded.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

tasks.withType<Test> {
    systemProperty("micronaut.http.client.read-timeout", System.getProperty("micronaut.http.client.read-timeout"))
}
