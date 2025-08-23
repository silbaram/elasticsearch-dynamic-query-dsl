package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

class BoostingQueryDsl {
    private val positiveQueries = SubQueryBuilders()
    private val negativeQueries = SubQueryBuilders()
    var negativeBoost: Double = 1.0

    fun positive(fn: SubQueryBuilders.() -> Unit) {
        positiveQueries.apply(fn)
    }

    fun negative(fn: SubQueryBuilders.() -> Unit) {
        negativeQueries.apply(fn)
    }

    internal fun buildPositiveQuery(): Query? {
        if (positiveQueries.size() == 0) {
            return null
        }
        if (positiveQueries.size() == 1) {
            var result: Query? = null
            positiveQueries.forEach { result = it }
            return result
        }
        return Query.of { q ->
            q.bool { b ->
                positiveQueries.forEach {
                    b.must(it)
                }
                b
            }
        }
    }

    internal fun buildNegativeQuery(): Query? {
        if (negativeQueries.size() == 0) {
            return null
        }
        if (negativeQueries.size() == 1) {
            var result: Query? = null
            negativeQueries.forEach { result = it }
            return result
        }
        return Query.of { q ->
            q.bool { b ->
                negativeQueries.forEach {
                    b.must(it)
                }
                b
            }
        }
    }
}

fun boostingQuery(fn: BoostingQueryDsl.() -> Unit): Query {
    val dsl = BoostingQueryDsl().apply(fn)
    return Query.of { q ->
        q.boosting { b ->
            dsl.buildPositiveQuery()?.let { b.positive(it) }
            dsl.buildNegativeQuery()?.let { b.negative(it) }
            b.negativeBoost(dsl.negativeBoost)
            b
        }
    }
}
