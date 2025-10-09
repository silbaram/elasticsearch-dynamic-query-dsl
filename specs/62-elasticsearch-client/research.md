# Research: Elasticsearch 클라이언트 구현

**Feature**: Elasticsearch 클라이언트
**Date**: 2025-10-03
**Status**: 완료

## 조사 목표

DSL로 작성된 쿼리를 Elasticsearch 클러스터에 전송하고 결과를 받는 클라이언트를 구현하기 위한 기술 조사.

## 1. Elasticsearch Java 클라이언트 선택

### 조사 질문
- 어떤 Elasticsearch 클라이언트 라이브러리를 사용할 것인가?
- Kotlin DSL과 가장 잘 통합되는 방법은?

### 대안 분석

#### Option 1: elasticsearch-java (공식 Java 클라이언트)
**장점**:
- Elastic 공식 지원
- 타입 안전한 API
- Jackson 통합으로 자동 직렬화/역직렬화
- Builder 패턴 제공
- Elasticsearch 8.x 완전 지원

**단점**:
- Java 중심 설계 (Kotlin DSL로 래핑 필요)
- 학습 곡선 존재

**적합성**: ✅ 최적

#### Option 2: High-Level REST Client
**장점**:
- 레거시 프로젝트에서 검증됨

**단점**:
- **Deprecated** (Elasticsearch 7.17 이후)
- 타입 안전성 부족
- 유지보수 종료

**적합성**: ❌ 사용 불가

#### Option 3: 직접 HTTP 클라이언트 사용
**장점**:
- 완전한 제어

**단점**:
- 유지보수 부담
- JSON 직렬화/역직렬화 직접 구현
- Elasticsearch 버전 업데이트 대응 어려움

**적합성**: ❌ 비효율적

### 최종 결정

**선택**: `elasticsearch-java 8.14.3`

**근거**:
1. 공식 지원 및 장기 유지보수 보장
2. 타입 안전성이 프로젝트 헌법 원칙(I. 타입 안정성 우선)과 일치
3. Jackson 통합으로 자동 정제 원칙 지원
4. Builder 패턴이 Kotlin DSL 래핑에 적합

## 2. 비동기 처리 전략

### 조사 질문
- 동기 검색만 지원할 것인가, 비동기도 지원할 것인가?
- 비동기를 지원한다면 어떤 방식을 사용할 것인가?

### 대안 분석

#### Option 1: Kotlin Coroutines
**장점**:
- Kotlin 생태계 표준
- `suspend` 함수로 간단한 API
- `withContext(Dispatchers.IO)`로 쉬운 통합
- 구조화된 동시성

**단점**:
- kotlinx-coroutines-core 의존성 추가

**적합성**: ✅ 최적

#### Option 2: Java CompletableFuture
**장점**:
- JDK 표준 (추가 의존성 없음)

**단점**:
- Kotlin에서 사용이 부자연스러움
- 에러 처리 복잡

**적합성**: ⚠️ 차선책

#### Option 3: RxJava
**장점**:
- 강력한 스트림 처리

**단점**:
- 무거운 의존성
- 학습 곡선 높음
- 프로젝트 범위 초과

**적합성**: ❌ 과도

### 최종 결정

**선택**: Kotlin Coroutines

**근거**:
1. Kotlin 프로젝트의 자연스러운 선택
2. 간단한 API로 사용성 향상
3. 헌법 원칙 V (라이브러리 우선)에 부합 - 의존성 최소화

**구현 방식**:
```kotlin
suspend fun <T> searchAsync(
    request: SearchRequest,
    clazz: Class<T>
): ElasticsearchSearchResponse<T> = withContext(Dispatchers.IO) {
    search(request, clazz)
}
```

## 3. 설정 관리 방법

### 조사 질문
- Elasticsearch 연결 정보를 어떻게 관리할 것인가?
- 개발/테스트/프로덕션 환경 분리는?

### 대안 분석

#### Option 1: 환경변수 + 빌더 패턴
**장점**:
- 12-factor app 원칙 준수
- 민감 정보 코드베이스 분리
- 테스트 시 쉬운 오버라이드

**단점**:
- 환경변수 설정 필요

**적합성**: ✅ 최적

#### Option 2: application.properties 파일
**장점**:
- Spring Boot 통합 용이

**단점**:
- 라이브러리 범위 초과
- 파일 관리 부담

**적합성**: ⚠️ 특정 프레임워크 의존

#### Option 3: 하드코딩
**장점**:
- 간단

**단점**:
- 보안 위험
- 유연성 부족

**적합성**: ❌ 사용 불가

### 최종 결정

**선택**: 환경변수 + 빌더 패턴

**근거**:
1. 라이브러리로서 프레임워크 중립적
2. 보안 및 유연성 확보
3. 테스트 용이성

**환경변수**:
- `ES_HOST`: Elasticsearch 호스트 (기본값: localhost)
- `ES_PORT`: 포트 (기본값: 9200)
- `ES_USERNAME`: 인증 사용자명 (선택)
- `ES_PASSWORD`: 인증 비밀번호 (선택)
- `ES_USE_SSL`: SSL 사용 여부 (기본값: false)

**구현**:
```kotlin
data class ElasticsearchClientConfig(
    val host: String = System.getenv("ES_HOST") ?: "localhost",
    val port: Int = System.getenv("ES_PORT")?.toIntOrNull() ?: 9200,
    val username: String? = System.getenv("ES_USERNAME"),
    val password: String? = System.getenv("ES_PASSWORD"),
    val useSsl: Boolean = System.getenv("ES_USE_SSL")?.toBoolean() ?: false
)
```

## 4. 응답 처리 패턴

### 조사 질문
- Elasticsearch 응답을 그대로 노출할 것인가, 래퍼로 감쌀 것인가?
- 공통 연산(총 개수, 히트 추출)을 어떻게 제공할 것인가?

### 대안 분석

#### Option 1: 래퍼 클래스
**장점**:
- 공통 연산 캡슐화
- 명확한 API
- IDE 자동완성 우수

**단점**:
- 추가 클래스 필요

**적합성**: ✅ 최적

#### Option 2: 원본 응답 직접 노출
**장점**:
- 최소 코드

**단점**:
- 사용성 저하
- elasticsearch-java 내부 API 노출

**적합성**: ❌ 사용성 부족

#### Option 3: 확장 함수
**장점**:
- Kotlin 관용적

**단점**:
- IDE 자동완성 제한
- 패키지 import 필요

**적합성**: ⚠️ 보조 수단

### 최종 결정

**선택**: 래퍼 클래스 + 확장 함수

**근거**:
1. 타입 안정성 및 사용성 향상 (헌법 원칙 I)
2. 명확한 API 제공 (헌법 원칙 V)
3. 확장 함수로 추가 편의 기능 제공

**구현**:
```kotlin
class ElasticsearchSearchResponse<T>(
    private val response: co.elastic.clients.elasticsearch.core.SearchResponse<T>
) {
    val totalHits: Long
        get() = response.hits().total()?.value() ?: 0

    val hits: List<SearchHit<T>>
        get() = response.hits().hits().map { SearchHit(it) }

    // ...
}
```

## 5. DSL 통합 방식

### 조사 질문
- 기존 쿼리 DSL을 클라이언트와 어떻게 통합할 것인가?

### 최종 결정

**선택**: SearchRequestBuilder DSL

**근거**:
- 기존 `query { }` DSL과 일관된 패턴
- 타입 안전한 빌더 체인
- 자동 정제 메커니즘 활용

**구현**:
```kotlin
fun <T> search(
    clazz: Class<T>,
    builder: SearchRequestBuilder.() -> Unit
): ElasticsearchSearchResponse<T> {
    val requestBuilder = SearchRequestBuilder().apply(builder)
    val request = requestBuilder.build()
    return search(request, clazz)
}
```

**사용 예시**:
```kotlin
val response = client.search<Product> {
    indices("products")
    query {
        matchQuery {
            field = "name"
            query = "laptop"
        }
    }
    size(20)
    sortByField("price", SortOrder.Desc)
}
```

## 6. 테스트 전략

### 조사 질문
- 실제 Elasticsearch 없이 테스트 가능한가?
- 통합 테스트는 어떻게 수행할 것인가?

### 최종 결정

**단위 테스트**: Mock 사용 (현재는 예제 테스트로 대체)
**통합 테스트**: Testcontainers + 실제 Elasticsearch

**근거**:
- 헌법 원칙 III (테스트 주도 개발)에 따라 실제 환경 검증 필요
- Testcontainers로 재현 가능한 테스트 환경 구축

## 요약

| 항목 | 결정 | 근거 |
|------|------|------|
| 클라이언트 | elasticsearch-java 8.14.3 | 공식 지원, 타입 안전성 |
| 비동기 | Kotlin Coroutines | Kotlin 표준, 간단한 API |
| 설정 | 환경변수 + 빌더 | 12-factor, 보안, 유연성 |
| 응답 처리 | 래퍼 클래스 | 사용성, IDE 지원 |
| DSL 통합 | SearchRequestBuilder | 일관성, 타입 안전성 |
| 테스트 | Testcontainers | 실제 환경 검증 |

**모든 결정은 프로젝트 헌법 원칙을 준수합니다.**
