plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.github.silbaram"
version = "v1.0.0-es8.14.2-3"
description = "Kotlin DSL for building Elasticsearch Query DSL mirroring Kibana-style JSON"

val isSnapshotVersion = version.toString().endsWith("SNAPSHOT")

repositories {
    mavenCentral()
}

val elasticsearchJavaVersion: String by project

dependencies {
    api("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

kotlin { 
    jvmToolchain(21)
}

java { 
    withSourcesJar() 
}

val dokkaHtml by tasks.existing(org.jetbrains.dokka.gradle.DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.flatMap { it.outputDirectory })
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(javadocJar)
            artifactId = "elasticsearch-dynamic-query-dsl"

            pom {
                name.set("elasticsearch-dynamic-query-dsl")
                description.set("A Kotlin DSL for building Elasticsearch queries dynamically and intuitively.")
                url.set("https://github.com/silbaram/elasticsearch-dynamic-query-dsl")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("silbaram")
                        name.set("sang jin park")
                        email.set("silbaram79@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/silbaram/elasticsearch-dynamic-query-dsl.git")
                    developerConnection.set("scm:git:ssh://git@github.com/silbaram/elasticsearch-dynamic-query-dsl.git")
                    url.set("https://github.com/silbaram/elasticsearch-dynamic-query-dsl")
                }
            }
        }
    }

    repositories {
        // ---------- OSSRH (Central sync) ----------
        maven {
            name = if (isSnapshotVersion) "OSSRH_SNAPSHOT" else "OSSRH_RELEASE"
            url = uri(
                if (isSnapshotVersion)
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }

        // ---------- GitHub Packages ----------
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/silbaram/elasticsearch-dynamic-query-dsl")
            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    } else {
        logger.warn("Signing disabled: missing SIGNING_KEY / SIGNING_PASSWORD")
    }
}
