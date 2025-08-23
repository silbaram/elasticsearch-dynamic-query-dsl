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
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.*

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

## 🛠️ 프로젝트 구조

- **`expansion/compound_queries`**: `boolQuery`와 같은 복합 쿼리의 시작점을 정의합니다.
- **`expansion/bool_clauses`**: `mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery` 확장 함수를 정의합니다.
- **`expansion/bool_clauses/item_level`**: `termQuery`, `termsQuery`, `existsQuery`, `rangeQuery` 등 개별 쿼리(Leaf Query)를 생성하는 함수들을 정의합니다. 각 함수는 입력값 유효성 검사를 포함합니다.
- **`helper`**: DSL의 핵심 로직을 담당하는 `SubQueryBuilders` 클래스를 포함합니다. 이 클래스는 `queries[...]` 구문을 처리하고, 단일/여러 쿼리 호출 모드를 관리하여 잘못된 사용을 방지합니다.

## 📜 라이선스


이 프로젝트는 Apache License 2.0을 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.