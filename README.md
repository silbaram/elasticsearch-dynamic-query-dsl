# Elasticsearch Dynamic Query DSL for Kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

코틀린의 타입-세이프 빌더(Type-Safe Builder)와 확장 함수를 활용하여, 복잡한 Elasticsearch 쿼리를 간결하고 직관적으로 작성할 수 있도록 돕는 DSL 라이브러리입니다.

이 라이브러리는 동적인 조건에 따라 쿼리가 생성되거나 제외되어야 하는 실무적인 요구사항을 쉽게 해결하는 데 중점을 둡니다.

## ✨ 주요 특징

- **직관적인 DSL**: 복잡한 JSON 구조 대신 코틀린다운 코드로 쿼리를 작성할 수 있습니다.
- **동적 쿼리 생성**: `term`이나 `range` 쿼리의 값이 `null`이거나 비어있으면, 해당 쿼리는 최종 결과에서 자동으로 제외됩니다. 더 이상 수많은 `if` 분기문이 필요 없습니다.
- **타입 안정성**: 코틀린 컴파일러의 지원을 받아 잘못된 쿼리 구조를 컴파일 시점에 방지합니다.
- **혼용 방지**: 단일 쿼리와 여러 쿼리를 묶는 `queries[...]` 구문을 혼용하여 발생할 수 있는 실수를 런타임 예외를 통해 방지합니다.
- **확장성**: 새로운 쿼리 타입을 쉽게 추가하고 기존 DSL에 통합할 수 있는 구조입니다.

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

## 🛠️ 프로젝트 구조

- `core`: DSL 핵심 유틸과 공통 빌더 (`QueryDsl`, `SubQueryBuilders`, `ElasticsearchJavaVersion`).
- `queries.compound`: `boolQuery`, `boostingQuery`, `constantScoreQuery` 등 복합(Compound) 쿼리 시작점.
- `queries.fulltext`: `matchQuery`, `matchBoolPrefixQuery` 등 전문(Full-text) 쿼리.
- `queries.termlevel`: `termQuery`, `termsQuery`, `existsQuery`, `rangeQuery` 등 용어/범위(Term-level) 쿼리.
- `clauses`: `mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery` 같은 Bool 절 확장.
- `clauses.integrations`: Bool 절 내부에서 사용하는 Compound 어댑터(`constantScoreQuery` 등).

## 📜 라이선스


이 프로젝트는 Apache License 2.0을 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.
