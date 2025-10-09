# Contract: Consumer Build Success

**Feature**: 064-transitive-dependency-fix
**Type**: Integration Contract
**Date**: 2025-10-08

## 계약 개요

이 계약은 라이브러리를 의존성으로 추가한 소비자 프로젝트에서 단일 의존성만으로 컴파일 및 테스트가 성공하는지 검증합니다.

## 계약 당사자

- **Provider**: `elasticsearch-dynamic-query-dsl` 라이브러리 (로컬 Maven 저장소)
- **Consumer**: 라이브러리를 사용하는 Gradle/Maven 프로젝트

## 전제 조건 (Preconditions)

1. 라이브러리가 로컬 Maven 저장소에 배포되어 있음 (`publishToMavenLocal` 완료)
2. POM Verification Contract가 통과되었음
3. 소비자 프로젝트가 다음 구조로 생성되어 있음:
   ```
   test-consumer/
   ├── build.gradle.kts
   ├── settings.gradle.kts
   └── src/test/kotlin/ConsumerTest.kt
   ```

## 계약 조건 (Postconditions)

### 필수 조건 (MUST)

#### 1. 단일 의존성으로 빌드 성공

**build.gradle.kts**:
```kotlin
dependencies {
    testImplementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")
    // elasticsearch-java를 명시적으로 추가하지 않음!
    // kotlinx-coroutines-core를 명시적으로 추가하지 않음!

    testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
    testImplementation("io.kotest:kotest-assertions-core:5.7.1")
}
```

**검증 명령**:
```bash
cd test-consumer
./gradlew clean build
# EXIT CODE: 0
```

#### 2. Query.Builder 클래스 접근 가능

**ConsumerTest.kt**:
```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConsumerTest : FunSpec({
    test("Query.Builder에 접근 가능해야 함") {
        // 이 코드가 컴파일되면 전이 의존성이 올바르게 포함된 것
        val builder = Query.Builder()
        builder shouldNotBe null
    }
})
```

**검증 명령**:
```bash
./gradlew test --tests ConsumerTest
# 컴파일 에러 없음
# 테스트 통과
```

#### 3. DSL 쿼리 빌더 사용 가능

**ConsumerTest.kt**:
```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery

class ConsumerTest : FunSpec({
    test("DSL로 Bool 쿼리 생성이 되어야 함") {
        val q = query {
            boolQuery {
                mustQuery {
                    termQuery {
                        field = "user.id"
                        value = "testuser"
                    }
                }
            }
        }

        q.isBool shouldBe true
        q.bool().must().size shouldBe 1
    }
})
```

**검증 명령**:
```bash
./gradlew test --tests "ConsumerTest"
# 모든 테스트 통과
```

#### 4. Coroutine API 사용 가능 (suspend 함수 접근)

```kotlin
import kotlinx.coroutines.runBlocking

class ConsumerTest : FunSpec({
    test("코루틴 API 사용 가능해야 함") {
        runBlocking {
            // kotlinx-coroutines-core가 전이 의존성으로 포함되었음을 검증
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
```

#### 5. 클래스패스에 중복 의존성 없음

**검증 명령**:
```bash
./gradlew dependencies --configuration testRuntimeClasspath | grep elasticsearch-java
# 결과: 정확히 하나의 버전만 표시되어야 함
# 예: co.elastic.clients:elasticsearch-java:8.14.3
```

## 실패 시나리오 및 기대 에러

### 시나리오 1: Query.Builder 접근 불가
**기대 에러**:
```
Cannot access class 'co.elastic.clients.elasticsearch._types.query_dsl.Query$Builder'.
Check your module classpath for missing or conflicting dependencies.
```

**원인**: POM 파일에 `elasticsearch-java` 의존성이 누락됨
**해결**: POM Verification Contract 재확인 필요

### 시나리오 2: kotlinx-coroutines 접근 불가
**기대 에러**:
```
Unresolved reference: runBlocking
```

**원인**: POM 파일에 `kotlinx-coroutines-core` 의존성이 누락됨
**해결**: POM Verification Contract 재확인 필요

### 시나리오 3: 버전 충돌
**기대 경고**:
```
Conflict found for the following module:
  - co.elastic.clients:elasticsearch-java (8.14.3 vs 8.10.0)
```

**원인**: 다른 의존성이 서로 다른 버전의 `elasticsearch-java`를 요구함
**해결**: Resolution strategy로 특정 버전 강제

## 검증 스크립트

### Bash 자동 검증 스크립트

```bash
#!/bin/bash

echo "=== Consumer Build Contract Verification ==="

cd test-consumer || exit 1

# 1. 클린 빌드
echo "Step 1: Clean build"
./gradlew clean build
if [ $? -ne 0 ]; then
    echo "❌ FAIL: Build failed"
    exit 1
fi
echo "✅ PASS: Build succeeded"

# 2. 테스트 실행
echo "Step 2: Run tests"
./gradlew test
if [ $? -ne 0 ]; then
    echo "❌ FAIL: Tests failed"
    exit 1
fi
echo "✅ PASS: Tests succeeded"

# 3. 의존성 확인
echo "Step 3: Check dependencies"
ELASTICSEARCH_COUNT=$(./gradlew dependencies --configuration testRuntimeClasspath | grep -c "elasticsearch-java")
if [ "$ELASTICSEARCH_COUNT" -eq 0 ]; then
    echo "❌ FAIL: elasticsearch-java not found in classpath"
    exit 1
fi
echo "✅ PASS: elasticsearch-java found in classpath"

COROUTINES_COUNT=$(./gradlew dependencies --configuration testRuntimeClasspath | grep -c "kotlinx-coroutines-core")
if [ "$COROUTINES_COUNT" -eq 0 ]; then
    echo "❌ FAIL: kotlinx-coroutines-core not found in classpath"
    exit 1
fi
echo "✅ PASS: kotlinx-coroutines-core found in classpath"

echo ""
echo "=== Contract Verification Complete ==="
```

## 완전한 ConsumerTest 예시

```kotlin
package test.consumer

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking

class ConsumerTest : FunSpec({
    context("전이 의존성 검증") {
        test("Query.Builder 클래스에 접근 가능해야 함") {
            val builder = Query.Builder()
            builder shouldNotBe null
        }

        test("Operator enum에 접근 가능해야 함") {
            val operator = Operator.And
            operator shouldNotBe null
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
    }

    context("DSL 기능 검증") {
        test("Term 쿼리 생성이 되어야 함") {
            val q = query {
                termQuery {
                    field = "user.id"
                    value = "testuser"
                }
            }

            q.isTerm shouldBe true
            q.term().field() shouldBe "user.id"
        }

        test("Bool 쿼리 생성이 되어야 함") {
            val q = query {
                boolQuery {
                    mustQuery {
                        termQuery {
                            field = "status"
                            value = "active"
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

        test("복잡한 중첩 Bool 쿼리 생성이 되어야 함") {
            val q = query {
                boolQuery {
                    mustQuery {
                        boolQuery {
                            shouldQuery {
                                termQuery {
                                    field = "tags"
                                    value = "kotlin"
                                }
                                termQuery {
                                    field = "tags"
                                    value = "elasticsearch"
                                }
                            }
                            minimumShouldMatch = "1"
                        }
                    }
                    filterQuery {
                        rangeQuery {
                            field = "created_at"
                            gte = "2025-01-01"
                        }
                    }
                    mustNotQuery {
                        termQuery {
                            field = "status"
                            value = "deleted"
                        }
                    }
                }
            }

            q.isBool shouldBe true
            val outerBool = q.bool()
            outerBool.must().size shouldBe 1
            outerBool.filter().size shouldBe 1
            outerBool.mustNot().size shouldBe 1

            val innerBool = outerBool.must().first().bool()
            innerBool.should().size shouldBe 2
            innerBool.minimumShouldMatch() shouldBe "1"
        }
    }
})
```

## 계약 성공 기준

모든 다음 조건이 충족되어야 계약이 성공한 것으로 간주됩니다:

- ✅ `./gradlew clean build` 성공 (EXIT CODE 0)
- ✅ `./gradlew test` 성공 (EXIT CODE 0)
- ✅ 모든 테스트 통과 (0 failures)
- ✅ 컴파일 에러 없음
- ✅ `Query.Builder` 접근 가능
- ✅ `kotlinx.coroutines.runBlocking` 접근 가능
- ✅ DSL 쿼리 빌더 정상 동작

## 계약 위반 시 대응

1. **POM 파일 재확인**: POM Verification Contract로 돌아가서 의존성 누락 확인
2. **Gradle 캐시 정리**: `~/.gradle/caches` 및 `~/.m2/repository` 정리 후 재빌드
3. **의존성 트리 출력**: `./gradlew dependencies --configuration testRuntimeClasspath` 실행하여 전체 의존성 트리 확인
4. **build.gradle.kts 수정**: 필요 시 `pom.withXml` 블록 추가

## 참고 문서

- [Gradle Dependency Management](https://docs.gradle.org/current/userguide/dependency_management.html)
- [Maven Transitive Dependencies](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Transitive_Dependencies)

---
**계약 상태**: ⏳ Pending Verification
**검증 일자**: TBD
**검증자**: TBD
