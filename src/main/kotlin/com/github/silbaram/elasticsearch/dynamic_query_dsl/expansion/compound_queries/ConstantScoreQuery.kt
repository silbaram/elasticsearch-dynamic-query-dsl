package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

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
                    filterQueries.forEach { b.must(it) }
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
