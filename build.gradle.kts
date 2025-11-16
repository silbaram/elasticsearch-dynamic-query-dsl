import com.vanniktech.maven.publish.SonatypeHost
import java.util.Base64
import org.gradle.plugins.signing.Sign

plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.silbaram"
version = "v1.0.0-es8.15.0-SNAPSHOT"

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

// Detect if signing is configured (우선순위: gradle.properties > 환경변수 > -P 옵션)
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
    repositories {
        // GitHub Packages (우선순위: gradle.properties > 환경변수)
        val ghUser = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
        val ghToken = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
        if (!ghUser.isNullOrBlank() && !ghToken.isNullOrBlank()) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/silbaram/elasticsearch-dynamic-query-dsl")
                credentials {
                    username = ghUser
                    password = ghToken
                }
            }
        } else {
            logger.lifecycle("GitHub Packages repository is disabled (missing GITHUB credentials).")
        }
    }
}

// vanniktech: 중앙 포털 사용
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    // 좌표와 POM 메타데이터를 이 블록에서 정의하여 단일 'maven' 퍼블리케이션만 사용
    coordinates(
        group as String,
        "elasticsearch-dynamic-query-dsl",
        version as String,
    )
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

// 로컬 배포 테스트 태스크 - 실제 업로드 없이 배포 준비 상태만 검증
tasks.register("verifyPublishSetup") {
    group = "verification"
    description = "로컬에서 Maven Central 배포 설정을 검증합니다 (실제 업로드 없음)"

    doLast {
        val checks = mutableListOf<Pair<String, Boolean>>()

        // 1. 버전 검증
        val versionCheck = !isSnapshotVersion
        checks.add("버전이 SNAPSHOT이 아님" to versionCheck)
        println("✓ 버전: $version ${if (versionCheck) "✅" else "❌ (SNAPSHOT 버전은 Central Portal에 배포할 수 없습니다)"}")

        // 2. POM 필수 정보 검증
        val pomName = "elasticsearch-dynamic-query-dsl"
        val pomDesc = description
        val pomUrl = "https://github.com/silbaram/elasticsearch-dynamic-query-dsl"
        checks.add("POM 메타데이터 존재" to (pomName.isNotEmpty() && !pomDesc.isNullOrEmpty() && pomUrl.isNotEmpty()))
        println("✓ POM 메타데이터: ${if (checks.last().second) "✅" else "❌"}")

        // 3. 서명 설정 검증
        checks.add("서명 설정" to isSigningConfiguredFlag)
        println("✓ PGP 서명 설정: ${if (isSigningConfiguredFlag) "✅" else "❌ (signing.secretKey 및 signing.password 필요)"}")
        if (isSigningConfiguredFlag) {
            println("  - SecretKey 존재: ${!signingSecretRaw.isNullOrBlank()}")
            println("  - Password 존재: ${!signingPasswordRaw.isNullOrBlank()}")
            val signingSource = when {
                findProperty("signing.secretKey") != null -> "gradle.properties"
                findProperty("signingKey") != null -> "gradle.properties (signingKey)"
                else -> "환경변수"
            }
            println("  - 출처: $signingSource")
        }

        // 4. Maven Central 자격증명 검증 (우선순위: gradle.properties > 환경변수)
        val centralUser = (findProperty("mavenCentralUsername") as String?) ?: System.getenv("MAVEN_CENTRAL_USERNAME")
        val centralPass = (findProperty("mavenCentralPassword") as String?) ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
        val centralCredsExist = !centralUser.isNullOrBlank() && !centralPass.isNullOrBlank()
        checks.add("Maven Central 자격증명" to centralCredsExist)
        println("✓ Maven Central 자격증명: ${if (centralCredsExist) "✅" else "❌ (gradle.properties 또는 환경변수 필요)"}")
        if (centralCredsExist) {
            val source = if (findProperty("mavenCentralUsername") != null) "gradle.properties" else "환경변수"
            println("  - 출처: $source")
        }

        // 5. 필수 아티팩트 검증 (sources, javadoc)
        val sourcesJarTask = tasks.findByName("sourcesJar")
        val javadocJarTask = tasks.findByName("javadocJar")
        checks.add("Sources JAR 태스크" to (sourcesJarTask != null))
        checks.add("Javadoc JAR 태스크" to (javadocJarTask != null))
        println("✓ Sources JAR: ${if (sourcesJarTask != null) "✅" else "❌"}")
        println("✓ Javadoc JAR: ${if (javadocJarTask != null) "✅" else "❌"}")

        println("\n" + "=".repeat(60))
        val allPassed = checks.all { it.second }
        if (allPassed) {
            println("✅ 모든 검증 통과! Maven Central 배포 준비가 완료되었습니다.")
            println("\n로컬 테스트를 위한 다음 단계:")
            println("  1. ./gradlew testPublishToCentral (dry-run 테스트)")
            println("  2. ./verify-publish-artifacts.sh (아티팩트 검증)")
            println("  3. ./gradlew publishToCentral (실제 배포)")
        } else {
            println("❌ 일부 검증 실패. 위 내용을 확인해주세요.")
            println("\n실패한 항목:")
            checks.filter { !it.second }.forEach { println("  - ${it.first}") }
        }
        println("=".repeat(60))

        if (!allPassed) {
            throw GradleException("배포 검증 실패. 설정을 확인해주세요.")
        }
    }
}

// 로컬 테스트용 dry-run 배포 태스크
tasks.register("testPublishToCentral") {
    group = "publishing"
    description = "Maven Central 배포를 로컬에서 테스트합니다 (실제 업로드 없이 아티팩트만 생성)"

    // 검증 먼저 실행
    dependsOn("verifyPublishSetup")

    // 빌드 및 서명까지만 수행
    dependsOn("build", "publishToMavenLocal")

    doLast {
        println("\n" + "=".repeat(60))
        println("✅ 로컬 Maven 저장소에 배포 완료!")
        println("\n생성된 아티팩트 확인:")
        println("  ~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/$version/")
        println("\n다음 스크립트로 아티팩트를 검증하세요:")
        println("  ./verify-publish-artifacts.sh")
        println("=".repeat(60))
    }
}

