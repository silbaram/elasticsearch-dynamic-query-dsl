# Data Model: 전이 의존성 문제 해결

**Feature**: 064-transitive-dependency-fix
**Date**: 2025-10-08
**Status**: Design Phase

## 개요

이 문서는 Gradle publishing 설정과 Maven POM 파일의 의존성 구조를 정의하고, 테스트 프로젝트에서 검증할 데이터 모델을 설명합니다.

## Entity: GradlePublishingConfiguration

Gradle의 Maven publishing 설정을 나타내는 개념적 엔티티입니다.

### 필드

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `component` | String | ✅ | 배포할 컴포넌트 ("java") |
| `artifactId` | String | ✅ | Maven artifactId |
| `groupId` | String | ✅ | Maven groupId |
| `version` | String | ✅ | 버전 정보 |
| `pomConfiguration` | PomConfiguration | ✅ | POM 메타데이터 설정 |
| `dependencies` | List<MavenPomDependency> | ✅ | 포함될 의존성 목록 |

### 검증 규칙

1. `component`는 반드시 `"java"`여야 함 (java-library 플러그인 사용)
2. `dependencies`는 최소 2개 이상이어야 함:
   - `elasticsearch-java` (scope: compile)
   - `kotlinx-coroutines-core` (scope: compile)
3. `implementation` 의존성은 POM에서 `runtime` 스코프로 매핑됨

### 예시

```kotlin
// build.gradle.kts
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])  // component = "java"
            artifactId = "elasticsearch-dynamic-query-dsl"
            groupId = "io.github.silbaram"
            version = "1.0.0"

            pom {
                // pomConfiguration 설정
                name.set("Elasticsearch Dynamic Query DSL")
                description.set("Type-safe Kotlin DSL for Elasticsearch queries")
                // ... 기타 메타데이터
            }
        }
    }
}
```

## Entity: MavenPomDependency

Maven POM 파일의 `<dependency>` 엘리먼트를 나타냅니다.

### 필드

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `groupId` | String | ✅ | Maven groupId |
| `artifactId` | String | ✅ | Maven artifactId |
| `version` | String | ✅ | 버전 (범위 가능) |
| `scope` | DependencyScope | ✅ | 의존성 스코프 |
| `optional` | Boolean | ❌ | 선택적 의존성 여부 (기본: false) |
| `exclusions` | List<Exclusion> | ❌ | 제외할 전이 의존성 |

### DependencyScope Enum

```kotlin
enum class DependencyScope {
    COMPILE,   // 컴파일 + 런타임 (기본값)
    RUNTIME,   // 런타임만
    PROVIDED,  // 컴파일만 (런타임 제외)
    TEST       // 테스트만
}
```

### 검증 규칙

1. **elasticsearch-java 의존성 규칙**:
   - `groupId`: "co.elastic.clients"
   - `artifactId`: "elasticsearch-java"
   - `version`: "8.14.3" (또는 그 이상)
   - `scope`: COMPILE (또는 null - 기본값이 compile)
   - **이유**: `Query`, `Query.Builder` 등이 라이브러리 공개 API에 노출됨

2. **kotlinx-coroutines-core 의존성 규칙**:
   - `groupId`: "org.jetbrains.kotlinx"
   - `artifactId`: "kotlinx-coroutines-core"
   - `version`: "1.7.3" (또는 그 이상)
   - `scope`: COMPILE (또는 null)
   - **이유**: `suspend` 함수가 공개 API에 포함됨

3. **jackson-databind 의존성 규칙** (선택적):
   - `groupId`: "com.fasterxml.jackson.core"
   - `artifactId`: "jackson-databind"
   - `scope`: RUNTIME
   - **이유**: 내부 구현에만 사용되므로 `implementation` 구성

### 예시 (XML)

```xml
<dependencies>
    <!-- API 의존성 → compile scope -->
    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>8.14.3</version>
        <scope>compile</scope>
    </dependency>

    <dependency>
        <groupId>org.jetbrains.kotlinx</groupId>
        <artifactId>kotlinx-coroutines-core</artifactId>
        <version>1.7.3</version>
        <scope>compile</scope>
    </dependency>

    <!-- Implementation 의존성 → runtime scope -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## Entity: TestConsumerProject

라이브러리를 소비하는 테스트 프로젝트의 구조를 나타냅니다.

### 디렉토리 구조

```
test-consumer/
├── build.gradle.kts          # Gradle 빌드 설정
├── settings.gradle.kts        # 프로젝트 설정
└── src/
    └── test/
        └── kotlin/
            └── ConsumerTest.kt  # 간단한 사용 테스트
```

### build.gradle.kts 구조

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenLocal()  // 로컬 Maven 저장소 우선
    mavenCentral()
}

dependencies {
    // 단 하나의 의존성만 추가
    testImplementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")

    // 테스트 프레임워크
    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}
```

### 검증 규칙

1. **단일 의존성 원칙**: `elasticsearch-dynamic-query-dsl`만 추가
2. **컴파일 성공**: `Query.Builder` 등의 클래스에 접근 가능해야 함
3. **테스트 통과**: 간단한 DSL 사용 테스트가 실행되어야 함

### ConsumerTest.kt 예시

```kotlin
package test.consumer

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConsumerTest : FunSpec({
    test("Query.Builder에 접근 가능해야 함") {
        // 이 코드가 컴파일되면 전이 의존성이 올바르게 포함된 것
        val q: Query = query {
            boolQuery {
                mustQuery {
                    termQuery {
                        field = "status"
                        value = "active"
                    }
                }
            }
        }

        q.isBool shouldBe true
    }
})
```

## POM 파일 구조

### 파일 위치

```
~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/
├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.jar
├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom      ← 검증 대상
├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.module
├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT-sources.jar
└── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT-javadoc.jar
```

### XML 스키마

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.silbaram</groupId>
    <artifactId>elasticsearch-dynamic-query-dsl</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Elasticsearch Dynamic Query DSL</name>
    <description>Type-safe Kotlin DSL for Elasticsearch queries</description>

    <dependencies>
        <!-- 이 섹션이 반드시 존재해야 함 -->
        <dependency>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
            <version>8.14.3</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>org.jetbrains.kotlinx</groupId>
            <artifactId>kotlinx-coroutines-core</artifactId>
            <version>1.7.3</version>
            <scope>compile</scope>
        </dependency>

        <!-- runtime 의존성도 포함될 수 있음 -->
    </dependencies>
</project>
```

### XPath 검증 쿼리

```xpath
# elasticsearch-java 의존성 존재 확인
//dependencies/dependency[groupId='co.elastic.clients' and artifactId='elasticsearch-java']

# elasticsearch-java 스코프 확인 (compile 또는 비어있어야 함)
//dependencies/dependency[groupId='co.elastic.clients' and artifactId='elasticsearch-java']/scope

# kotlinx-coroutines-core 의존성 존재 확인
//dependencies/dependency[groupId='org.jetbrains.kotlinx' and artifactId='kotlinx-coroutines-core']

# kotlinx-coroutines-core 스코프 확인
//dependencies/dependency[groupId='org.jetbrains.kotlinx' and artifactId='kotlinx-coroutines-core']/scope
```

## Gradle Metadata (.module) 구조

### 파일 형식

JSON 형식의 Gradle 모듈 메타데이터 (Gradle 5.0+)

```json
{
  "formatVersion": "1.1",
  "component": {
    "group": "io.github.silbaram",
    "module": "elasticsearch-dynamic-query-dsl",
    "version": "1.0.0-SNAPSHOT"
  },
  "variants": [
    {
      "name": "apiElements",
      "attributes": {
        "org.gradle.category": "library",
        "org.gradle.usage": "java-api"
      },
      "dependencies": [
        {
          "group": "co.elastic.clients",
          "module": "elasticsearch-java",
          "version": { "requires": "8.14.3" }
        },
        {
          "group": "org.jetbrains.kotlinx",
          "module": "kotlinx-coroutines-core",
          "version": { "requires": "1.7.3" }
        }
      ]
    },
    {
      "name": "runtimeElements",
      "attributes": {
        "org.gradle.category": "library",
        "org.gradle.usage": "java-runtime"
      },
      "dependencies": [
        {
          "group": "co.elastic.clients",
          "module": "elasticsearch-java",
          "version": { "requires": "8.14.3" }
        },
        {
          "group": "org.jetbrains.kotlinx",
          "module": "kotlinx-coroutines-core",
          "version": { "requires": "1.7.3" }
        },
        {
          "group": "com.fasterxml.jackson.core",
          "module": "jackson-databind",
          "version": { "requires": "2.15.2" }
        }
      ]
    }
  ]
}
```

### 검증 규칙

1. **apiElements variant**: `api` 의존성만 포함
2. **runtimeElements variant**: `api` + `implementation` 의존성 모두 포함
3. Gradle 프로젝트는 `.module` 파일을 POM보다 우선 사용

## 의존성 매핑 테이블

| Gradle 구성 | 라이브러리 | Maven POM 스코프 | Gradle Module Variant |
|-------------|-----------|-----------------|----------------------|
| `api` | elasticsearch-java | `compile` | apiElements + runtimeElements |
| `api` | kotlinx-coroutines-core | `compile` | apiElements + runtimeElements |
| `implementation` | jackson-databind | `runtime` | runtimeElements만 |
| `compileOnly` | (없음) | `provided` | N/A |
| `testImplementation` | kotest | (POM에 없음) | N/A |

## 성공 기준

### POM 파일 검증

- ✅ `<dependencies>` 섹션 존재
- ✅ `elasticsearch-java` 의존성 존재, scope=compile
- ✅ `kotlinx-coroutines-core` 의존성 존재, scope=compile
- ✅ XML 파싱 에러 없음

### Gradle 메타데이터 검증

- ✅ `apiElements` variant에 `elasticsearch-java`와 `kotlinx-coroutines-core` 포함
- ✅ `runtimeElements` variant에 모든 의존성 포함
- ✅ JSON 파싱 에러 없음

### 테스트 프로젝트 검증

- ✅ `./gradlew build` 성공
- ✅ `Query.Builder` 등의 클래스 접근 가능
- ✅ 테스트 실행 성공

---
**작성 완료일**: 2025-10-08
**다음 단계**: contracts/ 디렉토리 생성 및 계약 정의
