plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.github.silbaram"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

val elasticsearchClientVersion: String by project

dependencies {
    implementation("co.elastic.clients:elasticsearch-java:$elasticsearchClientVersion")

    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
