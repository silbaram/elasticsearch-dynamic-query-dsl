plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // 단일 의존성만 추가 - elasticsearch-java는 전이 의존성으로 자동 포함되어야 함
    testImplementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")

    // 테스트 프레임워크
    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
