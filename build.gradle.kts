import com.vanniktech.maven.publish.SonatypeHost
import java.util.Base64
import org.gradle.plugins.signing.Sign

plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.vanniktech.maven.publish") version "0.29.0"
}

group = "io.github.silbaram"
version = "1.0.0-es8.14.2-3"

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

// Detect if signing is configured (either via -P or ENV) and skip Sign tasks otherwise
val signingSecretRaw: String? =
    (findProperty("signing.secretKey") as String?)
        ?: (findProperty("signingKey") as String?)
        ?: System.getenv("SIGNING_KEY")
val signingPasswordRaw: String? =
    (findProperty("signing.password") as String?)
        ?: (findProperty("signingPassword") as String?)
        ?: System.getenv("SIGNING_PASSWORD")
val isSigningConfiguredFlag = !signingSecretRaw.isNullOrBlank() && !signingPasswordRaw.isNullOrBlank()

tasks.withType<Sign>().configureEach {
    onlyIf { isSigningConfiguredFlag }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["javadocJar"])
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
        // GitHub Packages
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

// vanniktech: 중앙 포털 사용
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

// 서명: -Psigning.secretKey / -Psigning.password 우선 사용, 없으면 기존 ENV 사용
signing {
    fun valueOrNull(s: String?): String? = s?.trim()?.takeIf { it.isNotEmpty() }
    val propSecret: String? = valueOrNull(findProperty("signing.secretKey") as String?)
    val propPassword: String? = valueOrNull(findProperty("signing.password") as String?)
    val envSecret: String? = valueOrNull((findProperty("signingKey") as String?) ?: System.getenv("SIGNING_KEY"))
    val envPassword: String? = valueOrNull((findProperty("signingPassword") as String?) ?: System.getenv("SIGNING_PASSWORD"))

    fun normalizeOrNull(raw: String): String? {
        val s0 = raw.trim().removeSurrounding("\"").removeSurrounding("'")
        if (s0.contains("BEGIN PGP PRIVATE KEY BLOCK")) return s0
        val decoded = runCatching { String(Base64.getDecoder().decode(s0.replace("\\n", "\n"))) }.getOrElse { null }
        val result = (decoded ?: s0).trim()
        return result.takeIf { it.isNotEmpty() }
    }
    val key = (propSecret ?: envSecret)?.let(::normalizeOrNull)
    val pwd = valueOrNull(propPassword ?: envPassword)

    if (key != null && pwd != null) {
        useInMemoryPgpKeys(key, pwd)
        sign(publishing.publications)
    } else {
        logger.lifecycle("PGP signing is not configured; skipping signing (secretPresent=${!((propSecret ?: envSecret).isNullOrBlank())}, passwordPresent=${!((propPassword ?: envPassword).isNullOrBlank())}).")
    }
}

// 편의 태스크
tasks.register("publishToGitHub") {
    dependsOn("publishMavenJavaPublicationToGitHubPackagesRepository")
}
tasks.register("publishToCentral") {
    doFirst { check(!isSnapshotVersion) { "Central Portal은 SNAPSHOT을 받지 않습니다. -SNAPSHOT 제거 후 재시도하세요." } }
    dependsOn("publish") // vanniktech가 중앙 포털 업로드를 처리
}

