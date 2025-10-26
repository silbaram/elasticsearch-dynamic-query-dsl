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
    // API dependencies (exposed to consumers)
    api("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Implementation dependencies (internal only)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Test dependencies
    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// Attach sources and Dokka-based javadoc artifacts for Maven Central
java {
    withSourcesJar()
}

// Create a javadocJar from Dokka HTML output (accepted by Maven Central)
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
            artifact(tasks["javadocJar"])
            // 일관된 배포 식별자 보장
            artifactId = "elasticsearch-dynamic-query-dsl"

            pom {
                name.set("elasticsearch-dynamic-query-dsl")
                description.set("A Kotlin DSL for building Elasticsearch queries dynamically and intuitively.")
                url.set("https://github.com/silbaram/elasticsearch-dynamic-query-dsl")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        // http -> https
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
                    // https/ssh 표준화
                    connection.set("scm:git:https://github.com/silbaram/elasticsearch-dynamic-query-dsl.git")
                    developerConnection.set("scm:git:ssh://git@github.com/silbaram/elasticsearch-dynamic-query-dsl.git")
                    url.set("https://github.com/silbaram/elasticsearch-dynamic-query-dsl")
                }
            }
        }
    }

    repositories {
        // Central: 릴리스일 때만 활성화
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
        // if (!isSnapshotVersion) {
        //     maven {
        //         name = "CentralPortal"
        //         url = uri("https://central.sonatype.com/api/v1/publisher/upload")
        //         credentials {
        //             // ~/.gradle/gradle.properties 또는 환경변수 사용 권장
        //             username = findProperty("centralUsername") as String? ?: System.getenv("CENTRAL_USERNAME")
        //             password = findProperty("centralPassword") as String? ?: System.getenv("CENTRAL_PASSWORD")
        //         }
        //     }
        // } else {
        //     logger.lifecycle("Central Portal publishing is disabled for snapshot version '$version'.")
        // }

        // GitHub Packages: 스냅샷/릴리스 공통
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
    val signingKey: String? = (findProperty("signingKey") as String?) ?: System.getenv("SIGNING_KEY")
    val signingPassword: String? = (findProperty("signingPassword") as String?) ?: System.getenv("SIGNING_PASSWORD")

    if (!isSnapshotVersion) {
        require(signingKey != null && signingPassword != null) {
            "Missing signingKey/signingPassword for release publication. Provide via ~/.gradle/gradle.properties or env vars."
        }
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    } else if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

// 편의 태스크
tasks.register("publishToGitHub") {
    dependsOn("publishMavenJavaPublicationToGitHubPackagesRepository")
}
tasks.register("publishToCentral") {
    doFirst { check(!isSnapshotVersion) { "Central Portal은 SNAPSHOT을 받지 않습니다. -SNAPSHOT 제거 후 재시도하세요." } }
    dependsOn("publishMavenJavaPublicationToCentralPortalRepository")
}