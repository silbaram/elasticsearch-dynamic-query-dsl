import java.util.Base64

plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
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
        // OSSRH(S01): 릴리스일 때만 활성화
        if (!isSnapshotVersion) {
            maven {
                name = "SonatypeOSSRH"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = findProperty("centralUsername") as String? ?: System.getenv("CENTRAL_USERNAME")
                    password = findProperty("centralPassword") as String? ?: System.getenv("CENTRAL_PASSWORD")
                }
            }
        } else {
            logger.lifecycle("OSSRH publishing is disabled for snapshot version '$version'.")
        }

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

// Nexus Publish 플러그인 설정(스테이징 close/release 자동화)
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set((findProperty("centralUsername") as String?) ?: System.getenv("CENTRAL_USERNAME"))
            password.set((findProperty("centralPassword") as String?) ?: System.getenv("CENTRAL_PASSWORD"))
        }
    }
}

signing {
    // 문자열/베이스64만 허용
    val signingKeyRaw: String? = (findProperty("signingKey") as String?) ?: System.getenv("SIGNING_KEY")
    val signingPassword: String? = (findProperty("signingPassword") as String?) ?: System.getenv("SIGNING_PASSWORD")

    // 필요 시 Base64 디코딩
    val signingKey: String? = signingKeyRaw?.let { raw ->
        try { String(Base64.getDecoder().decode(raw)) } catch (_: IllegalArgumentException) { raw }
    }

    fun isAsciiArmoredPrivateKey(s: String) =
        s.contains("BEGIN PGP PRIVATE KEY BLOCK") && s.contains("END PGP PRIVATE KEY BLOCK")

    // GitHub Actions에서만 서명 수행
    val isCi = (System.getenv("CI") == "true") || (System.getenv("GITHUB_ACTIONS") == "true")

    if (isCi) {
        if (!isSnapshotVersion) {
            require(signingKey != null && signingPassword != null) {
                "Missing signingKey/signingPassword for release publication."
            }
            require(isAsciiArmoredPrivateKey(signingKey)) {
                "SIGNING_KEY must be an ASCII-armored PGP private key."
            }
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        } else if (signingKey != null && signingPassword != null) {
            // 스냅샷은 선택적 서명
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }
    } else {
        logger.lifecycle("Signing is disabled locally. It only runs on CI (GitHub Actions).")
    }
}

// 편의 태스크
tasks.register("publishToGitHub") {
    dependsOn("publishMavenJavaPublicationToGitHubPackagesRepository")
}
tasks.register("publishToCentral") {
    doFirst { check(!isSnapshotVersion) { "OSSRH는 SNAPSHOT을 배포하지 않습니다. -SNAPSHOT 제거 후 재시도하세요." } }
    // 1) 업로드 → 2) 스테이징 close/release
    dependsOn("publishMavenJavaPublicationToSonatypeOSSRHRepository", "closeAndReleaseSonatypeStagingRepository")
}

