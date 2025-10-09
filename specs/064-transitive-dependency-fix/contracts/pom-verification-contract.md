# Contract: POM Verification

**Feature**: 064-transitive-dependency-fix
**Type**: Build Contract
**Date**: 2025-10-08

## 계약 개요

이 계약은 `publishToMavenLocal` 실행 후 생성된 Maven POM 파일에 필수 전이 의존성이 올바르게 포함되는지 검증합니다.

## 계약 당사자

- **Provider**: Gradle `maven-publish` 플러그인 + `java-library` 플러그인
- **Consumer**: Maven/Gradle 빌드 시스템을 사용하는 라이브러리 사용자

## 전제 조건 (Preconditions)

1. `build.gradle.kts`에 `java-library` 플러그인이 적용되어 있음
2. `elasticsearch-java`와 `kotlinx-coroutines-core`가 `api` 구성으로 선언되어 있음
3. `maven-publish` 플러그인이 `from(components["java"])`로 설정되어 있음
4. `./gradlew publishToMavenLocal` 명령이 성공적으로 실행됨

## 계약 조건 (Postconditions)

### 필수 조건 (MUST)

#### 1. POM 파일 존재
```bash
FILE_PATH=~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom

test -f "$FILE_PATH"
# EXIT CODE: 0 (파일 존재)
```

#### 2. XML 형식 유효성
```bash
# xmllint 또는 XML 파서로 검증
xmllint --noout "$FILE_PATH"
# EXIT CODE: 0 (유효한 XML)
```

#### 3. <dependencies> 섹션 존재
```xpath
//project/dependencies

# 결과: <dependencies> 노드가 반드시 존재해야 함
```

#### 4. elasticsearch-java 의존성 포함
```xpath
//dependencies/dependency[
    groupId='co.elastic.clients' and
    artifactId='elasticsearch-java'
]

# 결과: 1개의 <dependency> 노드 발견
```

**세부 검증**:
- `groupId`: "co.elastic.clients"
- `artifactId`: "elasticsearch-java"
- `version`: "8.14.3" 또는 그 이상
- `scope`: "compile" 또는 비어있음 (기본값이 compile)

#### 5. kotlinx-coroutines-core 의존성 포함
```xpath
//dependencies/dependency[
    groupId='org.jetbrains.kotlinx' and
    artifactId='kotlinx-coroutines-core'
]

# 결과: 1개의 <dependency> 노드 발견
```

**세부 검증**:
- `groupId`: "org.jetbrains.kotlinx"
- `artifactId`: "kotlinx-coroutines-core"
- `version`: "1.7.3" 또는 그 이상
- `scope`: "compile" 또는 비어있음

### 권장 조건 (SHOULD)

#### 6. implementation 의존성은 runtime 스코프
```xpath
//dependencies/dependency[
    groupId='com.fasterxml.jackson.core' and
    artifactId='jackson-databind'
]/scope

# 결과: "runtime"
```

#### 7. 중복 의존성 없음
```xpath
count(//dependencies/dependency[
    groupId='co.elastic.clients' and
    artifactId='elasticsearch-java'
])

# 결과: 1 (정확히 하나)
```

## 검증 스크립트

### Bash 스크립트 예시

```bash
#!/bin/bash

POM_FILE=~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom

echo "=== POM Verification Contract ==="

# 1. 파일 존재 확인
if [ ! -f "$POM_FILE" ]; then
    echo "❌ FAIL: POM file does not exist at $POM_FILE"
    exit 1
fi
echo "✅ PASS: POM file exists"

# 2. elasticsearch-java 의존성 확인
if grep -q "<groupId>co.elastic.clients</groupId>" "$POM_FILE" && \
   grep -q "<artifactId>elasticsearch-java</artifactId>" "$POM_FILE"; then
    echo "✅ PASS: elasticsearch-java dependency found"
else
    echo "❌ FAIL: elasticsearch-java dependency not found"
    exit 1
fi

# 3. kotlinx-coroutines-core 의존성 확인
if grep -q "<groupId>org.jetbrains.kotlinx</groupId>" "$POM_FILE" && \
   grep -q "<artifactId>kotlinx-coroutines-core</artifactId>" "$POM_FILE"; then
    echo "✅ PASS: kotlinx-coroutines-core dependency found"
else
    echo "❌ FAIL: kotlinx-coroutines-core dependency not found"
    exit 1
fi

# 4. compile 스코프 확인 (또는 스코프 없음 = 기본 compile)
SCOPE=$(xmllint --xpath "//dependencies/dependency[groupId='co.elastic.clients' and artifactId='elasticsearch-java']/scope/text()" "$POM_FILE" 2>/dev/null || echo "compile")
if [ "$SCOPE" = "compile" ] || [ "$SCOPE" = "" ]; then
    echo "✅ PASS: elasticsearch-java has compile scope (or default)"
else
    echo "⚠️  WARN: elasticsearch-java scope is $SCOPE (expected compile)"
fi

echo ""
echo "=== Contract Verification Complete ==="
```

### Kotlin 테스트 예시

```kotlin
package com.github.silbaram.elasticsearch.dynamic_query_dsl.build

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document

class PomVerificationContractTest : FunSpec({
    val pomFile = File(
        System.getProperty("user.home"),
        ".m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/" +
        "1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom"
    )

    test("POM 파일이 존재해야 함") {
        pomFile.exists() shouldBe true
    }

    test("POM 파일이 유효한 XML이어야 함") {
        val doc = parsePom(pomFile)
        doc shouldNotBe null
    }

    test("elasticsearch-java 의존성이 포함되어야 함") {
        val doc = parsePom(pomFile)
        val deps = getDependencies(doc)

        deps.any { dep ->
            dep.groupId == "co.elastic.clients" &&
            dep.artifactId == "elasticsearch-java"
        } shouldBe true
    }

    test("elasticsearch-java의 스코프가 compile이어야 함") {
        val doc = parsePom(pomFile)
        val deps = getDependencies(doc)

        val esDep = deps.first { dep ->
            dep.groupId == "co.elastic.clients" &&
            dep.artifactId == "elasticsearch-java"
        }

        // scope가 null이거나 "compile"이면 통과
        (esDep.scope == null || esDep.scope == "compile") shouldBe true
    }

    test("kotlinx-coroutines-core 의존성이 포함되어야 함") {
        val doc = parsePom(pomFile)
        val deps = getDependencies(doc)

        deps.any { dep ->
            dep.groupId == "org.jetbrains.kotlinx" &&
            dep.artifactId == "kotlinx-coroutines-core"
        } shouldBe true
    }
})

data class PomDependency(
    val groupId: String,
    val artifactId: String,
    val version: String?,
    val scope: String?
)

fun parsePom(file: File): Document {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    return builder.parse(file)
}

fun getDependencies(doc: Document): List<PomDependency> {
    val dependencies = mutableListOf<PomDependency>()
    val depNodes = doc.getElementsByTagName("dependency")

    for (i in 0 until depNodes.length) {
        val depNode = depNodes.item(i)
        val children = depNode.childNodes

        var groupId: String? = null
        var artifactId: String? = null
        var version: String? = null
        var scope: String? = null

        for (j in 0 until children.length) {
            val child = children.item(j)
            when (child.nodeName) {
                "groupId" -> groupId = child.textContent
                "artifactId" -> artifactId = child.textContent
                "version" -> version = child.textContent
                "scope" -> scope = child.textContent
            }
        }

        if (groupId != null && artifactId != null) {
            dependencies.add(PomDependency(groupId, artifactId, version, scope))
        }
    }

    return dependencies
}
```

## 실패 시나리오

### 시나리오 1: <dependencies> 섹션이 없음
**원인**: `from(components["java"])` 대신 수동 `artifact(jar)` 사용
**영향**: 소비자 프로젝트에서 컴파일 에러
**해결**: POM에 `pom.withXml` 블록으로 의존성 수동 추가

### 시나리오 2: scope가 runtime으로 잘못 설정됨
**원인**: Gradle Issue #1118 - 모든 의존성이 runtime으로 매핑
**영향**: Maven 프로젝트에서 컴파일 타임에 접근 불가
**해결**: `pom.withXml`로 scope를 명시적으로 "compile"로 수정

### 시나리오 3: 의존성이 누락됨
**원인**: `api` 대신 `implementation` 사용
**영향**: 전이 의존성이 전파되지 않음
**해결**: `build.gradle.kts`에서 `api` 구성으로 변경

## 계약 위반 시 대응

1. **진단**: POM 파일 내용을 출력하여 실제 구조 확인
   ```bash
   cat "$POM_FILE" | grep -A 10 "<dependencies>"
   ```

2. **수정**: `build.gradle.kts`의 publishing 블록에 `pom.withXml` 추가
   ```kotlin
   pom {
       // ... 기존 설정 ...

       withXml {
           val dependenciesNode = asNode().appendNode("dependencies")

           configurations.api.get().allDependencies.forEach { dep ->
               val dependencyNode = dependenciesNode.appendNode("dependency")
               dependencyNode.appendNode("groupId", dep.group)
               dependencyNode.appendNode("artifactId", dep.name)
               dependencyNode.appendNode("version", dep.version)
               dependencyNode.appendNode("scope", "compile")
           }
       }
   }
   ```

3. **재검증**: `./gradlew clean publishToMavenLocal` 재실행 후 계약 재확인

## 참고 문서

- [Gradle Java Library Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html)
- [Maven POM Reference](https://maven.apache.org/pom.html#Dependencies)
- [Gradle Publishing Maven](https://docs.gradle.org/current/userguide/publishing_maven.html)

---
**계약 상태**: ⏳ Pending Verification
**검증 일자**: TBD
**검증자**: TBD
