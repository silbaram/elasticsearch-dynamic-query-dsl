plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`

    // Central Publishing Portal 전용 플러그인
    id("com.vanniktech.maven.publish") version "0.34.0"

    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.github.silbaram"
// Maven 관례상 v 접두사는 제거 권장 (기능엔 영향 없음)
version = "1.0.0-es8.14.2-3"

description = "Kotlin DSL for building Elasticsearch Query DSL mirroring Kibana-style JSON"

repositories { mavenCentral() }

val elasticsearchJavaVersion: String by project

dependencies {
    api("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
kotlin { jvmToolchain(21) }
java { withSourcesJar() }

// ---- Central(신규 포털) 퍼블리싱 설정: Vanniktech ----
mavenPublishing {
    // 업로드 후 자동 Publish까지 하고 싶으면 automaticRelease = true
    publishToMavenCentral(automaticRelease = true)

    // 모든 publication 서명 (in-memory PGP)
    signAllPublications()

    // 좌표 고정 (⚠️ 위치 인자 사용 또는 정확한 이름 groupId)
    coordinates(
        "io.github.silbaram",                // groupId
        "elasticsearch-dynamic-query-dsl",   // artifactId
        version.toString()                   // version
    )

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

// ---- GitHub Packages 퍼블리싱(기존처럼 유지) ----
// Vanniktech는 내부적으로 maven-publish를 사용하므로 repositories 만 추가하면 같이 올라감.
publishing {
    repositories {
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

// (선택) GitHub 전용 편의 태스크
tasks.register("publishToGitHub") {
    dependsOn("publishAllPublicationsToGitHubPackagesRepository")
}
