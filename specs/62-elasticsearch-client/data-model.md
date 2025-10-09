# Data Model: Elasticsearch 클라이언트

**Feature**: Elasticsearch 클라이언트
**Date**: 2025-10-03

## 개요

Elasticsearch 클라이언트는 5개의 핵심 엔티티로 구성됩니다. 각 엔티티는 명확한 책임을 가지며, 타입 안전성과 자동 정제 원칙을 준수합니다.

## 엔티티 정의

### 1. ElasticsearchClientConfig

**목적**: Elasticsearch 클러스터 연결 설정을 관리합니다.

**속성**:
| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| host | String | Y | "localhost" | Elasticsearch 호스트 |
| port | Int | Y | 9200 | Elasticsearch 포트 |
| username | String? | N | null | 기본 인증 사용자명 |
| password | String? | N | null | 기본 인증 비밀번호 |
| useSsl | Boolean | Y | false | SSL/TLS 사용 여부 |

**검증 규칙**:
- port는 1-65535 범위
- username과 password는 함께 설정되거나 모두 null
- useSsl이 true면 포트는 일반적으로 9243

**관계**:
- ElasticsearchClientWrapper가 이 설정을 사용하여 클라이언트 생성

**상태 전이**: 불변(Immutable) - 생성 후 변경 불가

---

### 2. ElasticsearchClientWrapper

**목적**: Elasticsearch 클라이언트의 진입점. 검색, 인덱싱, 인덱스 관리 기능 제공.

**속성**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| config | ElasticsearchClientConfig | Y | 연결 설정 |
| client | ElasticsearchClient (private) | Y | 내부 ES 클라이언트 |

**메서드** (데이터 관점):
- `search(request, clazz)`: 검색 요청 → ElasticsearchSearchResponse
- `searchAsync(request, clazz)`: 비동기 검색 → ElasticsearchSearchResponse
- `index(index, id, document)`: 문서 색인 → IndexResponse
- `bulk(operations)`: 대량 작업 → BulkResponse
- `createIndex(name, settings, mappings)`: 인덱스 생성 → CreateIndexResponse
- `deleteIndex(name)`: 인덱스 삭제 → DeleteIndexResponse
- `indexExists(name)`: 인덱스 존재 확인 → Boolean

**검증 규칙**:
- 모든 요청 전에 클라이언트 연결 상태 확인
- 타입 파라미터 T는 Jackson 직렬화 가능해야 함

**관계**:
- ElasticsearchClientConfig 소유 (composition)
- SearchRequestBuilder를 사용하여 요청 구성
- ElasticsearchSearchResponse 생성

**상태 전이**:
```
[생성됨] --close()--> [종료됨]
```

---

### 3. SearchRequestBuilder

**목적**: 타입 안전한 검색 요청을 구성하는 DSL 빌더.

**속성**:
| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| indices | List<String> | N | emptyList() | 검색 대상 인덱스 |
| query | Query? | N | null | 검색 쿼리 (DSL) |
| size | Int? | N | null | 결과 개수 제한 |
| from | Int? | N | null | 페이징 오프셋 |
| sort | List<SortOptions> | N | emptyList() | 정렬 옵션 |
| sourceFilter | SourceFilter? | N | null | 소스 필드 필터 |

**검증 규칙**:
- indices가 비어있으면 모든 인덱스 검색
- size는 0 이상
- from은 0 이상
- query가 null이면 match_all 쿼리 사용

**관계**:
- Query 엔티티와 연관 (기존 DSL)
- SearchRequest로 변환

**상태 전이**: 빌더 패턴 - 메서드 체인으로 상태 구성

---

### 4. ElasticsearchSearchResponse<T>

**목적**: Elasticsearch 검색 결과를 타입 안전하게 래핑.

**속성**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| totalHits | Long | Y | 총 히트 수 |
| hits | List<SearchHit<T>> | Y | 검색 결과 리스트 |
| maxScore | Double? | N | 최고 스코어 |
| took | Long | Y | 검색 소요 시간 (ms) |

**파생 속성**:
- `isEmpty`: totalHits == 0
- `isNotEmpty`: totalHits > 0

**검증 규칙**:
- totalHits는 항상 0 이상
- hits 리스트 크기 ≤ totalHits

**관계**:
- SearchHit<T>의 컬렉션 소유
- 원본 SearchResponse를 내부적으로 보유

**상태 전이**: 불변(Immutable)

---

### 5. SearchHit<T>

**목적**: 개별 검색 결과 문서를 표현.

**속성**:
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| source | T? | N | 문서 소스 데이터 |
| id | String | Y | 문서 ID |
| index | String | Y | 인덱스 이름 |
| score | Double? | N | 관련성 스코어 |

**검증 규칙**:
- id는 빈 문자열 불가
- source는 SourceFilter에 의해 null 가능

**관계**:
- ElasticsearchSearchResponse의 일부
- 제네릭 타입 T는 사용자 정의 데이터 클래스

**상태 전이**: 불변(Immutable)

---

## 엔티티 관계 다이어그램

```
┌──────────────────────────────┐
│ ElasticsearchClientConfig    │
│ - host: String               │
│ - port: Int                  │
│ - username: String?          │
│ - password: String?          │
│ - useSsl: Boolean            │
└──────────────┬───────────────┘
               │ uses
               ▼
┌──────────────────────────────────────┐
│ ElasticsearchClientWrapper           │
│ - config: ElasticsearchClientConfig  │
│ + search(request, clazz)             │
│ + searchAsync(request, clazz)        │
│ + index(...)                         │
│ + bulk(...)                          │
│ + createIndex(...)                   │
└──────────────┬───────────────────────┘
               │ builds
               ▼
┌──────────────────────────────┐
│ SearchRequestBuilder         │◄────────┐
│ - indices: List<String>      │         │
│ - query: Query?              │         │ uses
│ - size: Int?                 │         │
│ - from: Int?                 │         │
│ - sort: List<SortOptions>    │    ┌────┴─────────┐
│ + build(): SearchRequest     │    │    Query     │
└──────────────┬───────────────┘    │  (기존 DSL)  │
               │ returns             └──────────────┘
               ▼
┌─────────────────────────────────────┐
│ ElasticsearchSearchResponse<T>      │
│ - totalHits: Long                   │
│ - hits: List<SearchHit<T>>          │
│ - maxScore: Double?                 │
│ - took: Long                        │
└──────────────┬──────────────────────┘
               │ contains
               ▼
┌──────────────────────────────┐
│ SearchHit<T>                 │
│ - source: T?                 │
│ - id: String                 │
│ - index: String              │
│ - score: Double?             │
└──────────────────────────────┘
```

## 데이터 흐름

### 검색 요청 흐름
```
1. 사용자 → SearchRequestBuilder DSL 작성
2. SearchRequestBuilder → Query 객체 포함
3. SearchRequestBuilder.build() → SearchRequest 생성
4. ElasticsearchClientWrapper.search() → Elasticsearch 클러스터 요청
5. Elasticsearch → 원본 SearchResponse 반환
6. ElasticsearchSearchResponse 래핑 → 사용자에게 반환
7. 사용자 → SearchHit<T> 컬렉션 접근
```

### 비동기 검색 흐름
```
1. 사용자 → suspend fun searchAsync() 호출
2. Coroutine → Dispatchers.IO에서 실행
3. search() 메서드 호출 (동기)
4. ElasticsearchSearchResponse 반환
```

## 타입 안정성 보장

### 제네릭 타입 T
- 모든 검색 메서드는 `Class<T>` 파라미터 요구
- Jackson이 JSON → T 자동 변환
- 컴파일 타임에 타입 체크

**예시**:
```kotlin
data class Product(val id: String, val name: String, val price: Double)

val response: ElasticsearchSearchResponse<Product> = client.search(Product::class.java) {
    // ...
}

val products: List<Product> = response.hits.mapNotNull { it.source }
```

## 자동 정제 규칙

### SearchRequestBuilder
- `indices`가 비어있으면 생략 → 모든 인덱스 검색
- `query`가 null이면 match_all로 자동 대체
- `size`, `from`이 null이면 Elasticsearch 기본값 사용
- `sort`가 비어있으면 생략
- `sourceFilter`가 null이면 모든 필드 반환

### 설정 검증
- ElasticsearchClientConfig는 생성 시점에 유효성 검증
- 잘못된 설정은 IllegalArgumentException 발생

## 확장 가능성

### 향후 추가 가능한 엔티티
- **ScrollContext**: Scroll API 지원
- **PointInTimeContext**: PIT 검색 지원
- **AggregationResult**: 집계 결과 래퍼
- **BulkOperationBuilder**: 대량 작업 DSL

### 관계 확장
- AggregationResult ← ElasticsearchSearchResponse에 추가
- ScrollContext ← ElasticsearchClientWrapper에서 관리
