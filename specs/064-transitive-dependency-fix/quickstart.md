# Quickstart: 전이 의존성 문제 해결

**Feature**: 064-transitive-dependency-fix
**Date**: 2025-10-08
**Audience**: 개발자

## 개요

이 가이드는 전이 의존성 문제를 진단하고 해결하는 단계별 절차를 제공합니다. 각 단계는 독립적으로 검증 가능하며, 실패 시 다음 단계로 진행합니다.

## 전제 조건

- JDK 21 설치됨
- Gradle Wrapper 사용 가능 (`./gradlew`)
- 기존 테스트가 모두 통과함

## 단계 1: 현재 상태 확인 (5분)

### 1.1 기존 테스트 실행

```bash
cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl
./gradlew clean test
```

**기대 결과**: 모든 테스트 통과 ✅

**실패 시**: 기존 테스트를 먼저 수정한 후 진행

### 1.2 현재 build.gradle.kts 확인

```bash
cat build.gradle.kts | grep -A 5 "dependencies {"
```

**확인 사항**:
- `java-library` 플러그인 적용 여부
- `elasticsearch-java`가 `api` 구성으로 선언되어 있는지
- `kotlinx-coroutines-core`가 `api` 구성으로 선언되어 있는지

**예상 내용**:
```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`  // ✅ 있어야 함
    `maven-publish`
    // ...
}

dependencies {
    api("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")  // ✅
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")              // ✅
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    // ...
}
```

## 단계 2: 로컬 Maven 저장소에 배포 (3분)

### 2.1 로컬 배포 실행

```bash
./gradlew clean publishToMavenLocal
```

**기대 결과**: `BUILD SUCCESSFUL` 메시지

**생성 파일 확인**:
```bash
ls -la ~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/
```

**확인할 파일**:
- `elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.jar`
- `elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom` ← **중요**
- `elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.module`
- `elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT-sources.jar`
- `elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT-javadoc.jar`

## 단계 3: POM 파일 검증 (5분)

### 3.1 POM 파일 출력

```bash
POM_FILE=~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom

cat "$POM_FILE"
```

### 3.2 의존성 섹션 확인

```bash
cat "$POM_FILE" | grep -A 50 "<dependencies>"
```

**기대 결과**: 다음과 같은 구조가 보여야 함

```xml
<dependencies>
  <dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>8.14.3</version>
    <scope>compile</scope>  <!-- 또는 scope 태그 없음 (기본값 compile) -->
  </dependency>

  <dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-core</artifactId>
    <version>1.7.3</version>
    <scope>compile</scope>  <!-- 또는 scope 태그 없음 -->
  </dependency>

  <!-- implementation 의존성은 runtime 스코프 -->
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

### 3.3 특정 의존성 검색

```bash
# elasticsearch-java 확인
grep -A 4 "elasticsearch-java" "$POM_FILE"

# kotlinx-coroutines-core 확인
grep -A 4 "kotlinx-coroutines-core" "$POM_FILE"
```

**✅ 성공 시**: 두 의존성이 모두 발견되고 scope가 `compile` (또는 없음)
→ **단계 4로 진행**

**❌ 실패 시**: `<dependencies>` 섹션이 없거나 의존성이 누락됨
→ **단계 5 (수정 방안)로 진행**

## 단계 4: 테스트 소비자 프로젝트 생성 (10분)

### 4.1 프로젝트 디렉토리 생성

```bash
cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl
mkdir -p test-consumer/src/test/kotlin/test/consumer
cd test-consumer
```

### 4.2 settings.gradle.kts 생성

```bash
cat > settings.gradle.kts << 'EOF'
rootProject.name = "test-consumer"
EOF
```

### 4.3 build.gradle.kts 생성

```bash
cat > build.gradle.kts << 'EOF'
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

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
EOF
```

### 4.4 ConsumerTest.kt 생성

```bash
cat > src/test/kotlin/test/consumer/ConsumerTest.kt << 'EOF'
package test.consumer

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking

class ConsumerTest : FunSpec({
    test("Query.Builder 접근 가능해야 함") {
        val builder = Query.Builder()
        builder shouldNotBe null
    }

    test("DSL로 Bool 쿼리 생성이 되어야 함") {
        val q = query {
            boolQuery {
                mustQuery {
                    termQuery {
                        field = "user.id"
                        value = "testuser"
                    }
                }
                filterQuery {
                    rangeQuery {
                        field = "age"
                        gte = 18
                    }
                }
            }
        }

        q.isBool shouldBe true
        q.bool().must().size shouldBe 1
        q.bool().filter().size shouldBe 1
    }

    test("Match 쿼리 생성이 되어야 함") {
        val q = query {
            matchQuery {
                field = "content"
                query = "elasticsearch"
                operator = Operator.And
            }
        }

        q.isMatch shouldBe true
        q.match().field() shouldBe "content"
        q.match().operator() shouldBe Operator.And
    }

    test("코루틴 API 사용 가능해야 함") {
        runBlocking {
            val q = query {
                termQuery {
                    field = "status"
                    value = "active"
                }
            }
            q.isTerm shouldBe true
        }
    }
})
EOF
```

### 4.5 Gradle Wrapper 복사 (선택)

```bash
cp -r ../gradle ./
cp ../gradlew ./
cp ../gradlew.bat ./
```

또는 Gradle Wrapper 생성:

```bash
gradle wrapper --gradle-version 8.10.2
```

### 4.6 빌드 및 테스트 실행

```bash
./gradlew clean test
```

**✅ 성공 시**: 모든 테스트 통과
→ **문제 해결 완료! 🎉**

**❌ 실패 시**: 컴파일 에러 발생
→ **단계 5 (수정 방안)로 진행**

**예상 에러**:
```
Cannot access class 'co.elastic.clients.elasticsearch._types.query_dsl.Query$Builder'.
Check your module classpath for missing or conflicting dependencies.
```

## 단계 5: 수정 방안 (15분)

### 5.1 POM에 의존성 명시적 추가

메인 프로젝트의 `build.gradle.kts` 파일을 수정합니다.

```bash
cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl
```

**build.gradle.kts 수정** (publishing 블록 내부):

```kotlin
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["javadocJar"])
            artifactId = "elasticsearch-dynamic-query-dsl"

            pom {
                name.set("Elasticsearch Dynamic Query DSL")
                description.set("Type-safe Kotlin DSL for composing Elasticsearch queries")
                url.set("https://github.com/silbaram/elasticsearch-dynamic-query-dsl")

                // 기존 라이선스, 개발자, SCM 설정...

                // ✅ 의존성 명시적 추가
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    // api 의존성 → compile scope
                    configurations.named("api").get().allDependencies.forEach { dep ->
                        if (dep.group != null && dep.name != null) {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dep.group)
                            dependencyNode.appendNode("artifactId", dep.name)
                            dependencyNode.appendNode("version", dep.version)
                            dependencyNode.appendNode("scope", "compile")
                        }
                    }

                    // implementation 의존성 → runtime scope
                    configurations.named("implementation").get().allDependencies.forEach { dep ->
                        if (dep.group != null && dep.name != null) {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dep.group)
                            dependencyNode.appendNode("artifactId", dep.name)
                            dependencyNode.appendNode("version", dep.version)
                            dependencyNode.appendNode("scope", "runtime")
                        }
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/silbaram/elasticsearch-dynamic-query-dsl")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

### 5.2 재배포 및 검증

```bash
# 1. 재배포
./gradlew clean publishToMavenLocal

# 2. POM 재확인
cat ~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom | grep -A 50 "<dependencies>"

# 3. 테스트 프로젝트 재실행
cd test-consumer
./gradlew clean test
```

**기대 결과**: 모든 테스트 통과 ✅

## 단계 6: 검증 및 커밋 (5분)

### 6.1 메인 프로젝트 테스트 재실행

```bash
cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl
./gradlew clean test
```

**기대 결과**: 기존 테스트 모두 통과 (회귀 방지 확인)

### 6.2 변경 사항 커밋

```bash
git add build.gradle.kts
git add test-consumer/
git status
# 변경 사항 확인 후:
# git commit -m "fix: POM 파일에 전이 의존성 명시적 추가"
```

## 문제 해결 (Troubleshooting)

### 문제 1: POM 파일에 <dependencies> 섹션이 없음

**원인**: `from(components["java"])`가 제대로 동작하지 않음

**해결**: 단계 5의 `pom.withXml` 블록 추가

### 문제 2: 의존성 스코프가 runtime으로 잘못 설정됨

**원인**: Gradle Issue #1118 - api 의존성이 runtime으로 매핑

**해결**: `pom.withXml` 블록에서 명시적으로 "compile" 스코프 지정

### 문제 3: 테스트 프로젝트에서 여전히 에러 발생

**진단 명령**:
```bash
cd test-consumer
./gradlew dependencies --configuration testRuntimeClasspath | grep elasticsearch-java
```

**확인 사항**:
- `elasticsearch-java`가 클래스패스에 있는지
- 버전 충돌이 있는지

**해결**:
```bash
# Gradle 캐시 정리
rm -rf ~/.gradle/caches
rm -rf ~/.m2/repository/io/github/silbaram

# 재배포
cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl
./gradlew clean publishToMavenLocal

# 테스트 재실행
cd test-consumer
./gradlew clean test --refresh-dependencies
```

### 문제 4: Kotlin 버전 불일치

**에러**:
```
Module was compiled with an incompatible version of Kotlin
```

**해결**: 테스트 프로젝트의 Kotlin 버전을 메인 프로젝트와 동일하게 맞춤
```kotlin
plugins {
    kotlin("jvm") version "2.0.20"  // 메인 프로젝트와 동일
}
```

## 체크리스트

작업 완료 전에 다음 항목을 확인하세요:

- [ ] 메인 프로젝트의 모든 테스트 통과
- [ ] `publishToMavenLocal` 성공
- [ ] POM 파일에 `elasticsearch-java` 의존성 포함 (scope=compile)
- [ ] POM 파일에 `kotlinx-coroutines-core` 의존성 포함 (scope=compile)
- [ ] 테스트 소비자 프로젝트 생성 완료
- [ ] 테스트 소비자 프로젝트의 모든 테스트 통과
- [ ] `Query.Builder` 접근 가능 확인
- [ ] 코루틴 API 사용 가능 확인
- [ ] 변경 사항 커밋

## 다음 단계

1. **GitHub Packages 배포**: 로컬 검증 완료 후 원격 저장소에 배포
2. **README 업데이트**: 의존성 추가 가이드 업데이트 (이미 완료됨)
3. **버전 태그**: 새 버전 릴리스 시 Git 태그 추가

## 참고 문서

- [contracts/pom-verification-contract.md](./contracts/pom-verification-contract.md)
- [contracts/consumer-build-contract.md](./contracts/consumer-build-contract.md)
- [data-model.md](./data-model.md)
- [research.md](./research.md)

---
**작성 완료일**: 2025-10-08
**예상 소요 시간**: 약 45분 (모든 단계 포함)
