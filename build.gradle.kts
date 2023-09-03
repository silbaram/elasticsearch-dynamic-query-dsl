plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.github.silbaram"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("co.elastic.clients:elasticsearch-java:8.9.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}