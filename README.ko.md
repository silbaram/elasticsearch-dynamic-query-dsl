# Kotlin용 Elasticsearch 동적 쿼리 DSL

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

타입 세이프 코틀린 DSL로 Elasticsearch 쿼리를 조합할 수 있는 라이브러리입니다. null/빈 값은 자동으로 제외되어 결과 JSON이 간결하고 유효하게 유지됩니다. English version is available in [`README.md`](README.md).

## 핵심 특징
- **코틀린 친화적 API**: JSON 문자열 대신 빌더 패턴으로 쿼리를 작성합니다.
- **안전한 생략 처리**: 불필요하거나 잘못된 입력을 자동으로 걸러냅니다.
- **폭넓은 쿼리 지원**: 전문 검색, term-level, span, compound, script, wrapper, pinned, rule, weighted_tokens 등 다양한 Elasticsearch DSL을 커버합니다.
- **재사용 가능한 헬퍼**: `SubQueryBuilders`로 bool 절 내부에서도 간단히 하위 쿼리를 누적할 수 있습니다.
- **테스트 검증**: Kotest + JUnit 5 스펙이 패키지 구조와 동일하게 구성되어 있어 예제와 검증을 동시에 제공합니다.

## 요구 사항
- JDK 17
- Gradle Wrapper (저장소에 포함)

## 시작하기
```bash
./gradlew clean build        # 전체 빌드 및 테스트
./gradlew test               # 반복 개발 시 빠른 테스트
./gradlew publishToMavenLocal # ~/.m2 로컬 배포
```

### 최소 예제
```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*

val q: Query = query {
    boolQuery {
        mustQuery { termQuery { field = "user.id"; value = "silbaram" } }
        filterQuery { rangeQuery { field = "age"; gte = 20; lt = 35 } }
        shouldQuery {
            queries[
                { termQuery { field = "tags"; value = "kotlin" } },
                { termQuery { field = "tags"; value = "search" } }
            ]
        }
        mustNotQuery { existsQuery { field = "deleted_at" } }
    }
}
```

## DSL 개요
### 기본 패턴
- 최상위는 `query { ... }` 또는 `queryOrNull { ... }` 를 사용합니다.
- `mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery` 등 절 전용 헬퍼로 서브 쿼리를 누적합니다.
- `SubQueryBuilders`는 `termQuery`, `rangeQuery`, `matchQuery`, `scriptQuery`, `scriptScoreQuery`, `wrapperQuery`, `pinnedQuery` 등 자주 쓰는 빌더를 바로 노출합니다.

### 자주 쓰는 쿼리 빌더
- **Term/Range**: `termQuery`, `termsQuery`, `rangeQuery`, `existsQuery`, `matchAllDsl`
- **전문 검색**: `matchQuery`, `matchPhrase`, `matchPhrasePrefix`, `matchBoolPrefix`, `multiMatchQuery`, `combinedFields`, `queryString`, `simpleQueryString`
- **Span/Interval**: `spanTermQuery`, `spanNearQuery`, `spanContainingQuery`, `intervals`

세부 예제는 `src/test/kotlin/.../queries/{termlevel,fulltext,span}` 경로의 Kotest 스펙을 참고하세요.

### 컴파운드/스코어링 빌더
- `boolQuery` + 절 헬퍼
- `functionScore` (field value factor, script score, random score, weight 등)
- `constantScore`, `boostingQuery`

## 특수 쿼리 모음
최근 추가된 스페셜 DSL은 다음과 같습니다.

```kotlin
// Script 쿼리 (inline/stored 모두 지원)
query {
    scriptQuery {
        inline(
            source = "doc['votes'].value > params.threshold",
            lang = "painless",
            params = mapOf("threshold" to 10)
        )
        boost = 1.2f
        _name = "votes-script"
    }
}

// Script score 쿼리 (organic 기본값은 match_all)
query {
    scriptScoreQuery {
        inline(source = "params.factor", params = mapOf("factor" to 2))
        minScore = 0.5f
    }
}

// Wrapper 쿼리: 원본 JSON 또는 base64 문자열 제공
query {
    wrapperQuery {
        rawJson("""{"match":{"status":"active"}}""")
    }
}

// Pinned 쿼리: 고정 문서와 일반 organic 쿼리 결합
query {
    pinnedQuery {
        ids("1", "2", "3")
        organic {
            matchQuery {
                field = "title"
                query = "elasticsearch"
            }
        }
    }
}

// Rule 쿼리: 룰셋과 organic 쿼리를 매칭 기준과 함께 연결
query {
    ruleQueryDsl {
        rulesetIds("featured")
        organic {
            matchQuery {
                field = "status"
                query = "active"
            }
        }
        matchCriteria(mapOf("channel" to "web"))
    }
}

// Weighted tokens 쿼리: 필드에 대해 가중 토큰을 제공
query {
    weightedTokensQuery {
        field = "title.embedding"
        tokens(
            "kotlin" to 1.0,
            "dsl" to 0.7
        )
        pruningConfig {
            tokensFreqRatioThreshold = 3
            tokensWeightThreshold = 0.2f
        }
    }
}
```

그 외 `knnQuery`, `percolateQuery`, `rankFeatureQuery`, `distanceFeatureQuery`, `ruleQuery` 등도 지원하며, 관련 예제는 `src/test/kotlin/.../queries/specialized`에서 확인할 수 있습니다.

## 테스트 & 품질 관리
- 필요 시 `./gradlew test --tests "패키지.클래스"`로 특정 스펙만 실행하세요.
- `./gradlew check`는 컴파일, 테스트, 추가 검증 작업을 한번에 수행합니다.
- 모든 스펙은 `context`/`should` 형태로 작성되어 읽기 쉽고, 프로덕션 패키지 구조와 동일하게 배치되어 있습니다.

## 기여 가이드
1. Conventional Commit 규칙에 맞춘 브랜치를 생성하세요 (예: `feat/pinned-query`).
2. 동작 변경 시 반드시 테스트를 추가하거나 수정합니다.
3. PR 전 `./gradlew check`가 통과하는지 확인합니다.
4. PR 본문에는 동기, DSL 사용 예시, 기대 JSON, 관련 이슈(`Fixes #123`)를 정리해 주세요.

## 라이선스
Apache License 2.0. 자세한 내용은 [LICENSE](LICENSE)를 참고하세요.
