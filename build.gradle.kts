plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "com.github.silbaram"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val elasticsearchJavaVersion: String by project

dependencies {
    implementation("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = group as String
            artifactId = "elasticsearch-dynamic-query-dsl"
            version = version as String
        }
    }
}
