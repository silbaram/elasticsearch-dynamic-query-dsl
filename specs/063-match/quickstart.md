# Quickstart: Match 쿼리 단위 테스트 및 리팩토링

**Feature**: 063-match
**Date**: 2025-10-06
**For**: 개발자

## 목표

이 가이드는 Match 쿼리의 포괄적인 단위 테스트를 추가하고 리팩토링하는 방법을 단계별로 설명합니다.

## 전제 조건

- ✅ JDK 21 설치
- ✅ Gradle 설치 (또는 gradlew 사용)
- ✅ Kotlin 플러그인이 설치된 IDE (IntelliJ IDEA 권장)
- ✅ Elasticsearch에 대한 기본 이해

## 빠른 시작 (5분)

### 1단계: 프로젝트 빌드 확인

```bash
# 프로젝트 루트에서
./gradlew clean build

# 예상 결과:
# BUILD SUCCESSFUL
```

### 2단계: 기존 Match Query 테스트 실행

```bash
# MatchQueryTest만 실행
./gradlew test --tests MatchQueryTest

# 예상 결과:
# MatchQueryTest > 18 tests PASSED
```

### 3단계: 기존 Match Query 코드 확인

```kotlin
// src/main/kotlin/.../MatchQueryDsl.kt
class MatchQueryDsl {
    var field: String? = null
    var query: String? = null
    var analyzer: String? = null
    var operator: Operator? = null
    // ... 13개 파라미터
}

fun Query.Builder.matchQuery(fn: MatchQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = MatchQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val q = dsl.query?.takeIf { it.isNotBlank() } ?: return this

    return this.match { m ->
        m.field(f)
        m.query(q)
        // ... optional 파라미터들
        m
    }
}
```

### 4단계: 간단한 Match Query 사용 예시

```kotlin
// Bool 쿼리 컨텍스트에서 사용
val query = query {
    boolQuery {
        mustQuery {
            matchQuery {
                field = "title"
                query = "elasticsearch"
                operator = Operator.And
                fuzziness = "AUTO"
                boost = 1.5F
            }
        }
    }
}

// 독립적으로 사용 (Bool 쿼리 없이)
val simpleQuery = query {
    matchQuery {
        field = "content"
        query = "search engine"
    }
}
```

## 상세 가이드

### A. 새로운 테스트 케이스 추가 (TC-018 ~ TC-021)

#### TC-018: 독립 Match 쿼리 생성 테스트

```kotlin
test("Bool 쿼리 없이 독립 Match 쿼리 생성이 되어야함") {
    // Given
    val query = query {
        matchQuery {
            field = "title"
            query = "elasticsearch"
        }
    }

    // Then
    query.isMatch shouldBe true
    query.match().field() shouldBe "title"
    query.match().query().stringValue() shouldBe "elasticsearch"
}
```

#### TC-019: field가 null/빈 값일 때 처리 테스트

```kotlin
test("field가 null이거나 빈 문자열일 때 Match 쿼리가 생성되지 않아야함") {
    // Given: field가 null
    val query1 = queryOrNull {
        matchQuery {
            field = null
            query = "test"
        }
    }

    // Then
    query1 shouldBe null

    // Given: field가 빈 문자열
    val query2 = queryOrNull {
        matchQuery {
            field = ""
            query = "test"
        }
    }

    // Then
    query2 shouldBe null
}
```

#### TC-020: 복수 파라미터 조합 테스트

```kotlin
test("복수 파라미터를 동시에 설정했을 때 모두 적용되어야함") {
    // Given
    val query = query {
        boolQuery {
            mustQuery {
                matchQuery {
                    field = "content"
                    query = "elasticsearch search"
                    operator = Operator.And
                    fuzziness = "AUTO"
                    analyzer = "standard"
                    boost = 1.5F
                    _name = "complex_match"
                }
            }
        }
    }

    // Then
    val match = query.bool().must().first().match()
    match.field() shouldBe "content"
    match.query().stringValue() shouldBe "elasticsearch search"
    match.operator() shouldBe Operator.And
    match.fuzziness() shouldBe "AUTO"
    match.analyzer() shouldBe "standard"
    match.boost() shouldBe 1.5F
    match.queryName() shouldBe "complex_match"
}
```

#### TC-021: 여러 Match 쿼리 추가 테스트 (기존 존재)

```kotlin
test("mustQuery DSL은 queries 블록 없이도 여러 match 쿼리를 추가할 수 있어야 함") {
    val query = query {
        boolQuery {
            mustQuery {
                matchQuery { field = "title"; query = "kotlin" }
                matchQuery { field = "body"; query = "dsl" }
            }
        }
    }

    val mustQuery = query.bool().must()
    mustQuery.size shouldBe 2
}
```

### B. 테스트 실행 및 검증

```bash
# 1. 신규 테스트 추가 후 실행
./gradlew test --tests MatchQueryTest

# 2. 테스트 리포트 확인
cat build/reports/tests/test/index.html

# 3. 전체 테스트 실행 (회귀 테스트)
./gradlew test

# 4. 빌드 및 패키징
./gradlew build
```

### C. 리팩토링 (필요 시)

현재 MatchQueryDsl은 이미 잘 작성되어 있으므로, 리팩토링은 선택 사항입니다.

#### 가능한 개선 사항

1. **KDoc 주석 추가**
```kotlin
/**
 * Elasticsearch Match 쿼리를 생성하기 위한 DSL 클래스
 *
 * Match 쿼리는 full-text 검색을 위한 가장 기본적인 쿼리 타입입니다.
 * 검색 텍스트를 분석기(analyzer)를 통해 토큰으로 변환한 후,
 * 해당 토큰들이 필드에 포함되어 있는지 검색합니다.
 *
 * @property field 검색 대상 필드명 (필수)
 * @property query 검색 텍스트 (필수)
 * @property operator 토큰 결합 방식 (AND/OR, 기본값: OR)
 * @property fuzziness 오타 허용도 ("AUTO", "0", "1", "2")
 * @property analyzer 쿼리 텍스트 분석기
 * ... (나머지 파라미터 설명)
 */
class MatchQueryDsl {
    // ...
}
```

2. **파라미터 그룹화 (가독성 향상)**
```kotlin
class MatchQueryDsl {
    // 필수 파라미터
    var field: String? = null
    var query: String? = null

    // 기본 옵션
    var analyzer: String? = null
    var operator: Operator? = null
    var minimumShouldMatch: String? = null

    // 퍼지 매칭 옵션
    var fuzziness: String? = null
    var prefixLength: Int? = null
    var maxExpansions: Int? = null
    var fuzzyTranspositions: Boolean? = null
    var fuzzyRewrite: String? = null

    // 고급 옵션
    var autoGenerateSynonymsPhraseQuery: Boolean? = null
    var lenient: Boolean? = null
    var zeroTermsQuery: ZeroTermsQuery? = null

    // 공통 옵션
    var boost: Float? = null
    var _name: String? = null
}
```

## 테스트 체크리스트

개발을 완료한 후 다음 체크리스트를 확인하세요:

- [ ] TC-001 ~ TC-017: 기존 테스트 모두 통과
- [ ] TC-018: 독립 Match 쿼리 생성 테스트 추가 및 통과
- [ ] TC-019: field null/빈 값 처리 테스트 추가 및 통과
- [ ] TC-020: 복수 파라미터 조합 테스트 추가 및 통과
- [ ] TC-021: 여러 Match 쿼리 추가 테스트 확인 (기존 존재)
- [ ] 전체 테스트 통과 (`./gradlew test`)
- [ ] 빌드 성공 (`./gradlew build`)
- [ ] 코드 리뷰 (헌법 원칙 준수 확인)
- [ ] 커밋 메시지 작성 (Conventional Commits)

## TDD 워크플로우

### Red-Green-Refactor 사이클

```bash
# Red: 테스트 작성 → 실패 확인
1. MatchQueryTest.kt에 TC-018 작성
2. ./gradlew test --tests MatchQueryTest
3. 새 테스트가 실패하는지 확인

# Green: 최소한의 코드로 테스트 통과
4. 필요시 MatchQueryDsl.kt 수정 (현재는 이미 구현되어 있음)
5. ./gradlew test --tests MatchQueryTest
6. 새 테스트가 통과하는지 확인

# Refactor: 코드 개선
7. 가독성, 구조 개선 (KDoc 주석, 파라미터 그룹화 등)
8. ./gradlew test --tests MatchQueryTest
9. 모든 테스트가 여전히 통과하는지 확인

# Repeat: TC-019, TC-020 반복
```

## 문제 해결

### 테스트 실패 시

**문제**: TC-018 테스트가 실패함
**원인**: `query.isMatch`가 false를 반환
**해결**: Bool 쿼리 없이 Match 쿼리만 사용했는지 확인

```kotlin
// ❌ 잘못된 예시
val query = query {
    boolQuery { matchQuery { ... } }
}

// ✅ 올바른 예시
val query = query {
    matchQuery { ... }
}
```

**문제**: TC-019 테스트가 실패함
**원인**: `queryOrNull`이 null을 반환하지 않음
**해결**: `queryOrNull` 함수 사용 (null 반환 가능)

```kotlin
// queryOrNull 사용
val query = queryOrNull {
    matchQuery { field = null; query = "test" }
}
query shouldBe null
```

### 빌드 실패 시

**문제**: Gradle 빌드가 실패함
**해결**:
```bash
# 1. 캐시 정리
./gradlew clean

# 2. 재빌드
./gradlew build

# 3. 의존성 문제 확인
./gradlew dependencies
```

## 다음 단계

1. ✅ 모든 테스트가 통과하면 커밋 생성
2. ✅ Pull Request 생성 (branch: 063-match → main)
3. ✅ 코드 리뷰 요청
4. ✅ CI/CD 파이프라인 확인
5. ✅ 병합 후 릴리스 노트 작성

## 참고 자료

- [Elasticsearch Match Query 공식 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html)
- [Kotest 공식 문서](https://kotest.io/)
- [프로젝트 헌법](/.specify/memory/constitution.md)
- [Feature Specification](./spec.md)
- [Test Contract](./contracts/match-query-test-contract.md)

---
**작성일**: 2025-10-06
**예상 소요 시간**: 1-2시간 (신규 테스트 3개 추가 + 검증)
