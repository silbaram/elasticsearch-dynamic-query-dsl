package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders

class ConstantScoreQueryDsl {
    private val filterQueries = SubQueryBuilders()
    var boost: Float? = null
    var _name: String? = null

    fun filterQuery(fn: SubQueryBuilders.() -> Any?) {
        val subQuery = SubQueryBuilders()
        val result = subQuery.fn()
        if (result is Query) {
            subQuery.addQuery(result)
        }
        filterQueries.addAll(subQuery)
    }

    internal fun buildFilterQuery(): Query? {
        return when (filterQueries.size()) {
            0 -> null
            1 -> {
                var result: Query? = null
                filterQueries.forEach { result = it }
                result
            }
            else -> Query.of { q ->
                q.bool { b ->
                    // 여러 필터는 의도에 맞게 filter 절로 묶는다
                    filterQueries.forEach { b.filter(it) }
                    b
                }
            }
        }
    }
}

fun Query.Builder.constantScoreQuery(fn: ConstantScoreQueryDsl.() -> Unit) {
    val dsl = ConstantScoreQueryDsl().apply(fn)
    val filterQuery = dsl.buildFilterQuery() ?: return
    this.constantScore { cs ->
        cs.filter(filterQuery)
        dsl.boost?.let { cs.boost(it) }
        dsl._name?.let { cs.queryName(it) }
        cs
    }
}
