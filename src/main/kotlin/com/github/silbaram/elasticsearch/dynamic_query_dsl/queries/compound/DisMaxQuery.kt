package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

class DisMaxQueryDsl {
    private val clauses = SubQueryBuilders()
    var tieBreaker: Double? = null
    var boost: Float? = null
    var _name: String? = null

    fun queries(fn: SubQueryBuilders.() -> Unit) {
        val subQuery = SubQueryBuilders().apply(fn)
        clauses.addAll(subQuery)
    }

    fun query(query: Query?) {
        clauses.addQuery(query)
    }

    fun query(fn: Query.Builder.() -> Unit) {
        clauses.addQuery(queryOrNull(fn))
    }

    internal fun buildQueries(): List<Query> {
        val results = mutableListOf<Query>()
        clauses.forEach { results.add(it) }
        return results
    }
}

fun Query.Builder.disMaxQuery(fn: DisMaxQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = DisMaxQueryDsl().apply(fn)
    val queries = dsl.buildQueries()
    if (queries.isEmpty()) return this

    return this.disMax { dm ->
        dm.queries(queries)
        dsl.tieBreaker?.let { dm.tieBreaker(it) }
        dsl.boost?.let { dm.boost(it) }
        dsl._name?.let { dm.queryName(it) }
        dm
    }
}

