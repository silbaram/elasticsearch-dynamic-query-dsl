plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`

    // ✅ Central Publishing Portal 지원 (업로드/퍼블리시/서명 전부 구성)
    id("com.vanniktech.maven.publish") version "0.34.0"

    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.github.silbaram"
version = "1.0.0-es8.14.2-3"

description = "Kotlin DSL for building Elasticsearch Query DSL mirroring Kibana-style JSON"

repositories { mavenCentral() }

val elasticsearchJavaVersion: String by project

dependencies {
    // API dependencies (exposed to consumers)
    api("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Implementation dependencies (internal only)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
kotlin { jvmToolchain(21) }

// 소스/자바독 JAR은 Vanniktech가 자동으로 붙인다.
// (원하면 다음 한 줄로 소스 Jar 보장)
java { withSourcesJar() }

// --- 중앙(Maven Central Portal) 퍼블리싱 설정 ---
// Vanniktech DSL
import com.vanniktech.maven.publish.SonatypeHost

mavenPublishing {
    // ✅ Central 포털로 업로드 + (원하면 자동 퍼블리시까지)
    // 자동 퍼블리시 원하면 automaticRelease = true 로
    publishToMavenCentral(
        // host = SonatypeHost.CENTRAL_PORTAL, // 기본이 CENTRAL_PORTAL
        automaticRelease = true
    )

    // ✅ 모든 퍼블리케이션 서명 (PGP in-memory)
    signAllPublications()

    // ✅ 좌표 명시 (artifactId 고정)
    // group/version은 상단의 group, version 값을 사용
    coordinates(
        group = "io.github.silbaram",
        artifactId = "elasticsearch-dynamic-query-dsl",
        version = version.toString()
    )

    // ✅ POM 메타데이터
    pom {
        name = "elasticsearch-dynamic-query-dsl"
        description = "A Kotlin DSL for building Elasticsearch queries dynamically and intuitively."
        url = "https://github.com/silbaram/elasticsearch-dynamic-query-dsl"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "silbaram"
                name = "sang jin park"
                email = "silbaram79@gmail.com"
            }
        }
        scm {
            url = "https://github.com/silbaram/elasticsearch-dynamic-query-dsl"
            connection = "scm:git:https://github.com/silbaram/elasticsearch-dynamic-query-dsl.git"
            developerConnection = "scm:git:ssh://git@github.com/silbaram/elasticsearch-dynamic-query-dsl.git"
        }
    }
}

// --- GitHub Packages 퍼블리싱 설정 ---
// Vanniktech는 내부적으로 maven-publish를 사용하므로,
// repositories 블록만 추가하면 같은 publication이 함께 업로드됨.
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/silbaram/elasticsearch-dynamic-query-dsl")
            credentials {
                // Actions에서 기본 제공되는 GITHUB_ACTOR/GITHUB_TOKEN 사용 또는 Secrets 매핑
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

