# Elasticsearch Dynamic Query DSL for Kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

코틀린의 타입-세이프 빌더(Type-Safe Builder)와 확장 함수를 활용하여, 복잡한 Elasticsearch 쿼리를 간결하고 직관적으로 작성할 수 있도록 돕는 DSL 라이브러리입니다.

이 라이브러리는 동적인 조건에 따라 쿼리가 생성되거나 제외되어야 하는 실무적인 요구사항을 쉽게 해결하는 데 중점을 둡니다.

## 📚 목차

- [✨ 주요 특징](#-주요-특징)
- [⚡ 빠른 시작](#-빠른-시작)
- [🚀 사용 방법](#-사용-방법)
  - [1. 기본 Bool 쿼리](#1-기본-bool-쿼리)
  - [2. 단일 쿼리와 여러 쿼리](#2-단일-쿼리와-여러-쿼리)
  - [3. 동적 쿼리 제외](#3-동적-쿼리-제외)
  - [4. match_bool_prefix 쿼리](#4-match_bool_prefix-쿼리)
  - [5. match_phrase 구문 검색](#5-match_phrase-구문-검색)
  - [6. match_phrase_prefix 구문 접두어](#6-match_phrase_prefix-구문-접두어)
  - [7. 멀티필드 구문 검색 (multi_match type=phrase)](#7-멀티필드-구문-검색-multi_match-typephrase)
  - [8. function_score 쿼리 (점수 조작)](#8-function_score-쿼리-점수-조작)
- [⚙️ 성능/튜닝 팁](#-성능튜닝-팁)
- [🛠️ 프로젝트 구조](#️-프로젝트-구조)
- [📜 라이선스](#-라이선스)

## ✨ 주요 특징

- **직관적인 DSL**: 복잡한 JSON 구조 대신 코틀린다운 코드로 쿼리를 작성할 수 있습니다.
- **동적 쿼리 생성**: `term`이나 `range` 쿼리의 값이 `null`이거나 비어있으면, 해당 쿼리는 최종 결과에서 자동으로 제외됩니다. 더 이상 수많은 `if` 분기문이 필요 없습니다.
- **타입 안정성**: 코틀린 컴파일러의 지원을 받아 잘못된 쿼리 구조를 컴파일 시점에 방지합니다.
- **혼용 방지**: 단일 쿼리와 여러 쿼리를 묶는 `queries[...]` 구문을 혼용하여 발생할 수 있는 실수를 런타임 예외를 통해 방지합니다.
- **확장성**: 새로운 쿼리 타입을 쉽게 추가하고 기존 DSL에 통합할 수 있는 구조입니다.
- **구문 검색 지원**: `match_phrase`, `match_phrase_prefix`, `multi_match(type=phrase)`를 통해 순서·근접성 기반 검색과 접두어 구문 검색을 간결하게 작성합니다.
- **고급 점수 조작**: `function_score` 쿼리를 통해 필드 값, 스크립트, 거리/시간 기반 감쇠 함수 등을 활용한 복합적인 점수 계산을 직관적으로 구성할 수 있습니다.

## ⚡ 빠른 시작

1) 빌드/테스트

```bash
./gradlew clean build
```

2) 로컬 배포(선택)

```bash
./gradlew publishToMavenLocal
```

3) 최소 예제

```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.*

val q: Query = query {
    boolQuery {
        mustQuery {
            queries[
                matchPhraseQuery("message", "this is a test"),
                matchPhrasePrefixQuery("path", "/api/ad"),
                multiMatchPhraseQuery("quick brown fox", listOf("title^2", "body"))
            ]
        }
    }
}
```

## 🚀 사용 방법

### 1. 기본 Bool 쿼리

`boolQuery` 블록 안에서 `must`, `filter`, `should`, `mustNot` 절을 사용하여 쿼리를 구성합니다.

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*

// ...

val query = Query.Builder()
    .boolQuery {
        // must 절
        mustQuery {
            termQuery(field = "user.id", value = "silbaram")
        }

        // filter 절 (스코어 계산에 영향을 주지 않음)
        filterQuery {
            rangeQuery(field = "age", gte = 20, lt = 30)
        }

        // should 절 (하나 이상 만족)
        shouldQuery {
            queries[
                termQuery(field = "tags", value = "kotlin"),
                termQuery(field = "tags", value = "elasticsearch")
            ]
        }
        
        // mustNot 절
        mustNotQuery {
            existsQuery(field = "deleted_at")
        }
    }
    .build()
```

### 2. 단일 쿼리와 여러 쿼리

- **단일 쿼리**: 람다 블록 안에 쿼리 함수를 직접 호출합니다.
- **여러 쿼리**: `queries[...]` 구문으로 여러 쿼리 함수를 감싸줍니다.

```kotlin
// 단일 쿼리 추가
mustQuery {
    termQuery(field = "status", value = "published")
}

// 여러 쿼리를 AND 조건으로 추가
mustQuery {
    queries[
        termQuery(field = "category", value = "tech"),
        existsQuery(field = "author")
    ]
}
```

### 3. 동적 쿼리 제외

쿼리 생성 함수의 인자값이 유효하지 않으면(`null` 또는 빈 문자열), 해당 쿼리는 최종 `bool` 절에서 자동으로 제외됩니다.

```kotlin
val keyword: String? = null // 외부에서 받은 값이 null일 경우
val category: String = "tech"

val query = Query.Builder()
    .boolQuery {
        filterQuery {
            queries[
                // keyword가 null이므로 이 termQuery는 생성되지 않음
                termQuery(field = "title", value = keyword),
                
                // category는 유효한 값이므로 이 쿼리는 생성됨
                termQuery(field = "category", value = category)
            ]
        }
    }
    .build()

// 최종적으로 생성된 filter 절에는 category에 대한 termQuery 하나만 포함됩니다.
```

### 4. match_bool_prefix 쿼리

입력 문장의 마지막 토큰을 접두(prefix)로 처리하여 자동 완성/부분 일치 검색에 유용합니다. `query`가 `null`이거나 빈 문자열이면 쿼리는 생성되지 않습니다.

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchBoolPrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator

val query = Query.Builder()
    .boolQuery {
        mustQuery {
            matchBoolPrefixQuery(
                field = "title",
                query = "quick brown f",
                operator = Operator.And,
                minimumShouldMatch = "2",
                analyzer = "standard"
            )
        }
    }
    .build()
```

최상위 쿼리로도 사용할 수 있습니다:

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchBoolPrefixQuery

val q = query {
    matchBoolPrefix(
        field = "message",
        query = "quick brown f"
    )
}
// q 는 { "match_bool_prefix": { "message": "quick brown f" } } 와 동일한 Query
```

동작 요약(공식 문서 반영):
- 입력을 분석한 뒤 마지막 토큰은 prefix 쿼리, 나머지 토큰은 term 쿼리로 해석되어 bool 쿼리를 구성합니다.
- match_phrase_prefix와 달리 구문(phrase) 순서를 강제하지 않으며, 토큰은 문서 내 임의의 위치에 나타나도 매칭됩니다.
- 예: `"quick brown f"` → `quick`, `brown`은 term, `f`는 prefix로 간주되어 should 절로 결합됩니다.

공식 JSON 예시와 개념적 등가 bool 쿼리:

```json
{
  "query": {
    "match_bool_prefix": {
      "message": "quick brown f"
    }
  }
}
```

위 쿼리는 분석 결과 토큰이 `quick`, `brown`, `f`일 때 아래와 유사하게 동작합니다(개념적 표현):

```json
{
  "query": {
    "bool": {
      "should": [
        { "term":   { "message": "quick" }},
        { "term":   { "message": "brown" }},
        { "prefix": { "message": "f" }}
      ]
    }
  }
}
```

추가 예제

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchBoolPrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator

// 1) AND 결합 + 최소 일치 수
val q1 = query {
    boolQuery {
        mustQuery {
            matchBoolPrefixQuery(
                field = "title",
                query = "quick brown f",
                operator = Operator.And,           // 모든 토큰이 있어야 함(마지막은 prefix)
                minimumShouldMatch = "2"           // 최소 토큰 2개 이상 일치
            )
        }
        filterQuery {
            // term/exists/range 등과 자유롭게 조합
        }
    }
}

// 2) 여러 필드 대상의 OR 검색 (자동완성 시 자주 사용)
val q2 = query {
    boolQuery {
        shouldQuery {
            queries[
                matchBoolPrefixQuery(field = "title", query = "kotlin d"),
                matchBoolPrefixQuery(field = "description", query = "kotlin d")
            ]
        }
        // 필요 시 tie-break를 위해 boost 조절 가능
    }
}

// 3) 최상위 쿼리로 간단히 사용
val q3 = query {
    matchBoolPrefix(field = "message", query = "quick brown f")
}
```

베스트 프랙티스
- 필드 타입: `text` 필드에 사용하세요. `keyword`는 분석되지 않으므로 prefix 동작과 맞지 않습니다.
- 멀티필드: `title` 같은 `text` 필드에 `title.keyword`(정확 일치용)를 함께 둘 수 있지만, `match_bool_prefix`에는 `text` 쪽을 사용하세요.
- 분석기 선택: 기본 `standard` 외에 한국어는 `nori` 등 언어 분석기 사용을 검토하세요. 토큰화 결과가 매칭 품질에 직결됩니다.
- 자동완성 품질: 보다 공격적인 프리픽스 매칭이 필요하면 인덱스 측면에서 `edge_ngram` 기반의 서제스트/커스텀 분석기를 고려하세요.
- 퍼지 매칭: `match_bool_prefix`는 fuzziness 옵션을 제공하지 않습니다. 오탈자 허용이 필요하면 별도의 `matchQuery`(+fuzziness) 등과 조합하세요.
- 순서 제약: 구문 순서를 강제하려면 `match_phrase_prefix` 사용을 고려하세요.

트러블슈팅
- 쿼리가 생성되지 않음: DSL 함수에 전달한 `query`가 `null` 또는 빈 문자열이면 동적으로 제외됩니다. 최상위로는 `matchBoolPrefix(...)`를, bool 내부 동적 제외 시에는 `matchBoolPrefixQuery(...)`를 사용하세요.
- 매칭이 약함/지나침: `operator`(AND/OR)와 `minimumShouldMatch`를 조정해 재현율/정밀도를 균형 있게 맞추세요.
- 기대와 다른 토큰화: 분석기 설정(예: `analyzer = "standard"`/`nori`)과 인덱스 매핑을 재검토하세요.

옵션 요약:

| 옵션 | 타입 | 설명 |
|---|---|---|
| `field` | String | 대상 필드 (필수) |
| `query` | String? | 검색어; null/빈 문자열은 제외 처리 |
| `operator` | Operator? | 토큰 결합 방식(And/Or) |
| `minimumShouldMatch` | String? | 최소 일치 토큰 수(예: "2") |
| `analyzer` | String? | 분석기 이름 |
| `fuzziness` | String? | 마지막 토큰 제외 term들만 퍼지 적용(AUTO 등) |
| `prefixLength` | Int? | 퍼지에서 고정 접두 길이(마지막 prefix에는 미적용) |
| `maxExpansions` | Int? | 퍼지 확장 최대치(마지막 prefix에는 미적용) |
| `fuzzyTranspositions` | Boolean? | 전치 허용(마지막 prefix에는 미적용) |
| `fuzzyRewrite` | String? | 퍼지 rewrite 전략(마지막 prefix에는 미적용) |
| `boost` | Float? | 가중치 |
| `_name` | String? | 쿼리 식별용 이름 |

### 5. match_phrase 구문 검색

문구의 순서와 근접성을 따르는 구문 검색입니다. `slop`으로 허용 간격을 조정할 수 있고, `zeroTermsQuery`로 불용어만 남는 경우의 동작을 정의합니다. `query`가 null/빈 문자열이면 동적으로 제외됩니다.

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchPhraseQuery
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery

val q = query {
    boolQuery {
        mustQuery {
            matchPhraseQuery(
                field = "message",
                query = "this is a test",
                slop = 0,
                analyzer = "standard",
                zeroTermsQuery = ZeroTermsQuery.None
            )
        }
    }
}
```

등가 JSON 예시:

```json
{
  "query": {
    "match_phrase": {
      "message": {
        "query": "this is a test",
        "slop": 0,
        "analyzer": "standard"
      }
    }
  }
}
```

예제 바로가기: [5. match_phrase 구문 검색](#5-match_phrase-구문-검색)

옵션 표

| 옵션 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `field` | String | - | 대상 필드(필수) |
| `query` | String? | - | 검색어; null/빈 문자열이면 제외 |
| `analyzer` | String? | 필드 검색 분석기 | 질의어 분석기 지정 |
| `slop` | Int? | 0 | 허용 간격(자리바꿈은 2로 계산) |
| `zeroTermsQuery` | ZeroTermsQuery? | None | 토큰 소거 시 동작(None/All) |
| `boost` | Float? | 1.0 | 가중치 |
| `_name` | String? | - | 쿼리 식별용 이름 |

### 6. match_phrase_prefix 구문 접두어

구문 순서를 유지하면서 마지막 토큰만 접두어로 확장합니다. 경로/식별자 등에도 유용합니다.

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchPhrasePrefix

val q = query {
    matchPhrasePrefix(
        field = "path",
        query = "/api/ad"
    )
}
```

옵션: `analyzer`, `slop`, `zeroTermsQuery`, `maxExpansions`, `boost`, `_name`

등가 JSON 예시:

```json
{
  "query": {
    "match_phrase_prefix": {
      "path": {
        "query": "/api/ad",
        "slop": 0
      }
    }
  }
}
```

예제 바로가기: [6. match_phrase_prefix 구문 접두어](#6-match_phrase_prefix-구문-접두어)

옵션 표

| 옵션 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `field` | String | - | 대상 필드(필수) |
| `query` | String? | - | 검색어; null/빈 문자열이면 제외 |
| `analyzer` | String? | 필드 검색 분석기 | 질의어 분석기 지정 |
| `slop` | Int? | 0 | 구문 허용 간격 |
| `zeroTermsQuery` | ZeroTermsQuery? | None | 토큰 소거 시 동작(None/All) |
| `maxExpansions` | Int? | 50 | 접두어 확장 최대치 |
| `boost` | Float? | 1.0 | 가중치 |
| `_name` | String? | - | 쿼리 식별용 이름 |

### 7. 멀티필드 구문 검색 (multi_match type=phrase)

여러 필드에 구문 의미로 검색합니다. 필드 가중치는 `^`로 지정하세요(`title^2`). `query`가 비거나 `fields`가 비면 동적으로 제외됩니다.

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.multiMatchPhrase

val q = query {
    multiMatchPhrase(
        query = "quick brown fox",
        fields = listOf("title^2", "body"),
        slop = 2
    )
}
```

등가 JSON 예시:

```json
{
  "query": {
    "multi_match": {
      "query": "quick brown fox",
      "type": "phrase",
      "fields": ["title^2", "body"],
      "slop": 2
    }
  }
}
```

## ⚙️ 성능/튜닝 팁

- 분석기 선택: 검색 대상이 자연어면 `text` + 언어별 분석기(`standard`, `nori` 등)를, 경로/식별자/코드면 `keyword` 또는 `analyzer = "keyword"`를 고려하세요. 불용어(stop)로 토큰이 모두 제거될 수 있으므로 필요 시 `zeroTermsQuery = All`을 사용합니다.
- Slop 가이드: `slop = 0`은 정확한 구문 일치입니다. 작은 오탈/간격 허용은 `1~2`를 권장합니다. 자리바꿈(transposed terms)은 슬롭 2로 계산됩니다. 큰 값은 후보 증가로 성능 저하와 정밀도 하락을 유발할 수 있습니다.
- Prefix 주의: `match_phrase_prefix`/`match_bool_prefix`는 접두어 확장으로 비용이 커질 수 있습니다. `maxExpansions`로 제한하고, 대규모 자동완성은 인덱스 측면 최적화(`edge_ngram` 분석기, `index_prefixes`)를 검토하세요.
- 매핑 최적화(참고): 구문/접두어 성능이 중요하면 텍스트 필드에 사전 계산을 켭니다.
  - `index_phrases: true` → 구문 검색 가속화
  - `index_prefixes` → 접두어 검색 가속화

예시 매핑(개념):

```json
{
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "index_phrases": true,
        "index_prefixes": { "min_chars": 1, "max_chars": 10 }
      }
    }
  }
}
```

예제 바로가기: [7. 멀티필드 구문 검색](#7-멀티필드-구문-검색-multi_match-typephrase)

옵션 표

| 옵션 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `query` | String? | - | 검색어; null/빈 문자열이면 제외 |
| `fields` | List<String> | - | 대상 필드 목록(비어있으면 제외) |
| `analyzer` | String? | 필드 검색 분석기 | 질의어 분석기 지정 |
| `slop` | Int? | 0 | 구문 허용 간격 |
| `zeroTermsQuery` | ZeroTermsQuery? | None | 토큰 소거 시 동작(None/All) |
| `boost` | Float? | 1.0 | 가중치 |
| `_name` | String? | - | 쿼리 식별용 이름 |

### 8. function_score 쿼리 (점수 조작)

복잡한 점수 계산 로직을 통해 검색 결과의 순위를 세밀하게 조정할 수 있습니다. 여러 점수 계산 함수를 조합하여 비즈니스 요구사항에 맞는 정확한 랭킹을 구현할 수 있습니다.

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.functionScoreQuery
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.json.JsonData

val query = query {
    functionScoreQuery {
        // 기본 검색 쿼리
        query {
            boolQuery {
                mustQuery {
                    termQuery(field = "status", value = "published")
                }
                filterQuery {
                    rangeQuery(field = "created_date", gte = "2023-01-01")
                }
            }
        }
        
        // 평점 기반 점수 조정
        function {
            fieldValueFactorQuery(
                field = "rating",
                factor = 1.5,
                modifier = "ln2p",
                missing = 1.0  // rating이 없는 문서는 1.0으로 처리
            )
        }
        
        // 프리미엄 문서에 가중치 부여
        function {
            filter(Query.of { it.term { t -> t.field("is_premium").value(true) } })
            weightQuery(2.0)
        }
        
        // 커스텀 스크립트로 인기도 점수 계산
        function {
            scriptScoreQuery(
                source = "Math.log(2 + doc['view_count'].value) * params.popularity_factor",
                params = mapOf("popularity_factor" to JsonData.of(0.8))
            )
        }
        
        // 점수 결합 방식 설정
        scoreMode = FunctionScoreMode.Sum      // 함수들의 점수를 합산
        boostMode = FunctionBoostMode.Multiply // 원본 점수와 곱셈
        maxBoost = 10.0                        // 최대 점수 제한
        minScore = 0.5                         // 최소 점수 임계값
    }
}
```

#### 지원하는 점수 함수들

**1. Field Value Factor**
```kotlin
function {
    fieldValueFactorQuery(
        field = "popularity_score",
        factor = 1.2,
        modifier = "sqrt",  // none, log, log1p, log2p, ln, ln1p, ln2p, sqrt, square, reciprocal
        missing = 0.0       // 필드가 없을 때 기본값
    )
}
```

**2. Script Score**
```kotlin
// 인라인 스크립트
function {
    scriptScoreQuery(
        source = "_score * Math.max(1, doc['boost_multiplier'].value)",
        params = mapOf("factor" to JsonData.of(1.5))
    )
}

// 저장된 스크립트
function {
    scriptScoreStoredQuery(
        id = "popularity_calculator",
        params = mapOf("base_score" to JsonData.of(1.0))
    )
}
```

**3. Weight (가중치)**
```kotlin
function {
    // 특정 조건의 문서에만 가중치 적용
    filter(Query.of { it.term { t -> t.field("featured").value(true) } })
    weightQuery(1.8)
}
```

**4. Random Score**
```kotlin
function {
    randomScoreQuery(
        seed = "daily_random_${LocalDate.now()}",  // 일별 동일한 랜덤 순서
        field = "user_id"  // 사용자별 개인화된 랜덤
    )
}
```

#### 점수 결합 방식

**Score Mode** (여러 함수의 점수를 어떻게 결합할지):
- `Sum`: 모든 함수 점수를 합산 (기본값)
- `Multiply`: 모든 함수 점수를 곱셈
- `Avg`: 함수 점수들의 평균
- `First`: 첫 번째 매칭되는 함수의 점수
- `Max`/`Min`: 최대/최소 함수 점수

**Boost Mode** (원본 쿼리 점수와 함수 점수를 어떻게 결합할지):
- `Multiply`: 원본 점수 × 함수 점수 (기본값)
- `Replace`: 함수 점수로 완전 대체
- `Sum`: 원본 점수 + 함수 점수
- `Avg`/`Max`/`Min`: 평균/최대/최소값 사용

#### 실전 활용 예시

**전자상거래 상품 랭킹**
```kotlin
val productRanking = query {
    functionScoreQuery {
        query {
            multiMatchQuery("노트북", listOf("name^2", "description"))
        }
        
        function {
            // 평점이 높을수록 점수 증가
            fieldValueFactorQuery("rating", "log1p", 1.2)
        }
        
        function {
            // 리뷰 수가 많을수록 점수 증가 (단, 로그 스케일)
            fieldValueFactorQuery("review_count", "ln1p", 0.8)
        }
        
        function {
            // 할인 상품에 보너스 점수
            filter(Query.of { it.range { r -> r.field("discount_rate").gt(JsonData.of(0)) } })
            weightQuery(1.3)
        }
        
        function {
            // 재고가 적으면 점수 하락
            scriptScoreQuery(
                source = "_score * Math.max(0.5, Math.min(1.0, doc['stock_quantity'].value / 10.0))"
            )
        }
        
        scoreMode = FunctionScoreMode.Sum
        boostMode = FunctionBoostMode.Multiply
    }
}
```

**콘텐츠 개인화 추천**
```kotlin
val personalizedContent = query {
    functionScoreQuery {
        query {
            boolQuery {
                shouldQuery {
                    queries[
                        termQuery("category", userPreferredCategory),
                        termQuery("tags", userInterests)
                    ]
                }
            }
        }
        
        function {
            // 최신 콘텐츠일수록 높은 점수
            scriptScoreQuery(
                source = "_score * Math.exp(-(System.currentTimeMillis() - doc['published_date'].value.millis) / params.decay_rate)",
                params = mapOf("decay_rate" to JsonData.of(7 * 24 * 3600 * 1000)) // 7일 감쇠
            )
        }
        
        function {
            // 사용자가 팔로우하는 작성자의 콘텐츠 부스트
            filter(Query.of { it.terms { t -> t.field("author_id").terms { terms -> 
                followingAuthorIds.forEach { terms.value(it) }
                terms
            }}})
            weightQuery(2.5)
        }
        
        scoreMode = FunctionScoreMode.Sum
        boostMode = FunctionBoostMode.Sum
    }
}
```

#### 성능 최적화 팁

- **Script Score 주의**: 스크립트는 비용이 높으므로 가능한 한 필드 기반 함수 사용을 권장합니다.
- **필터 활용**: `filter`를 사용해 특정 조건의 문서에만 함수를 적용하면 성능이 향상됩니다.
- **적절한 Score Mode**: 대부분의 경우 `Sum`이나 `Multiply`가 적절하며, `First`는 성능상 이점이 있습니다.
- **최대/최소 점수 설정**: `maxBoost`와 `minScore`로 점수 범위를 제한하여 예측 가능한 결과를 얻을 수 있습니다.

## 🛠️ 프로젝트 구조

- `core`: DSL 핵심 유틸과 공통 빌더 (`QueryDsl`, `SubQueryBuilders`, `ElasticsearchJavaVersion`).
- `queries.compound`: `boolQuery`, `boostingQuery`, `constantScoreQuery`, `functionScoreQuery` 등 복합(Compound) 쿼리 시작점.
- `queries.fulltext`: `matchQuery`, `matchBoolPrefixQuery` 등 전문(Full-text) 쿼리.
- `queries.termlevel`: `termQuery`, `termsQuery`, `existsQuery`, `rangeQuery` 등 용어/범위(Term-level) 쿼리.
- `clauses`: `mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery` 같은 Bool 절 확장.
- `clauses.integrations`: Bool 절 내부에서 사용하는 Compound 어댑터(`constantScoreQuery` 등).

## 📜 라이선스


이 프로젝트는 Apache License 2.0을 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.
