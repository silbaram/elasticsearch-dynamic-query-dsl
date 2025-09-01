package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders

class BoostingQueryDsl {
    private val positiveQueries = SubQueryBuilders()
    private val negativeQueries = SubQueryBuilders()
    var negativeBoost: Double = 1.0

    fun positive(fn: SubQueryBuilders.() -> Any?) {
        val subQuery = SubQueryBuilders()
        val result = fn(subQuery)
        if (result is Query) {
            subQuery.addQuery(result)
        }
        positiveQueries.addAll(subQuery)
    }

    fun negative(fn: SubQueryBuilders.() -> Any?) {
        val subQuery = SubQueryBuilders()
        val result = fn(subQuery)
        if (result is Query) {
            subQuery.addQuery(result)
        }
        negativeQueries.addAll(subQuery)
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

fun Query.Builder.boostingQuery(fn: BoostingQueryDsl.() -> Unit) {
    val dsl = BoostingQueryDsl().apply(fn)
    this.boosting { b ->
        dsl.buildPositiveQuery()?.let { b.positive(it) }
        dsl.buildNegativeQuery()?.let { b.negative(it) }
        b.negativeBoost(dsl.negativeBoost)
        b
    }
}
