plugins {
    kotlin("jvm") version "1.9.0"
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "com.github.silbaram"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://artifacts.elastic.co/maven")
}

val elasticsearchJavaVersion: String by project

dependencies {
    implementation("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
