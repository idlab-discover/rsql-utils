plugins {
    kotlin("jvm") version "1.9.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.nstdio:rsql-parser:2.2.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("org.apache.commons:commons-text:1.11.0")
    testImplementation("io.vertx:vertx-core:4.5.1")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}