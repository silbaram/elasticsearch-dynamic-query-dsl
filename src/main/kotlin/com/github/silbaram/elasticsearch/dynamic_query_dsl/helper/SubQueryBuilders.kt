package com.github.silbaram.elasticsearch.dynamic_query_dsl.helper

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

/**
 * 여러 clause 확장함수에서 재사용하는 Query 수집기.
 * - 기존 `queries[...]` 구문을 지원
 * - 단일 쿼리 추가용 addQuery / unaryPlus 지원
 */
class SubQueryBuilders {
    private val collectedQueries = mutableListOf<Query>()

    // queries[...] 사용을 위해 자기 자신 반환
    val queries: SubQueryBuilders
        get() = this

    // queries[ q1, q2 ] 를 처리
    operator fun get(vararg queries: Query?) {
        this.collectedQueries.addAll(queries.filterNotNull())
    }

    // 개별 쿼리 추가 (람다 안에서 호출할 때 사용)
    fun addQuery(query: Query?) {
        query?.let { collectedQueries.add(it) }
    }

    /**
     * 중첩된 bool 쿼리를 생성합니다. 이 함수는 mustQuery, shouldQuery 등 내부에서 호출되어야 합니다.
     * @param fn 중첩될 bool 쿼리를 설정하는 람다입니다.
     * @return 생성된 bool 쿼리를 담은 Query 객체를 반환합니다.
     */
    fun boolQuery(fn: BoolQuery.Builder.() -> Unit): Query {
        return Query.of { q ->
            q.bool { b ->
                b.fn()
                b
            }
        }
    }

    // + 쿼리 문법을 선호한다면 아래를 사용:
    operator fun Query?.unaryPlus() {
        this?.let { collectedQueries.add(it) }
    }

    fun size(): Int = collectedQueries.size

    fun forEach(action: (Query) -> Unit) = collectedQueries.forEach(action)
}