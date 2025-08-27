package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.FunctionScoreQueryDsl
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

fun SubQueryBuilders.functionScoreQuery(
    fn: FunctionScoreQueryDsl.() -> Unit,
): SubQueryBuilders {
    val dsl = FunctionScoreQueryDsl().apply(fn)
    val innerQuery = dsl.buildQuery()
    val fsQuery = Query.of { q ->
        q.functionScore { fs ->
            innerQuery?.let { fs.query(it) }
            val funcs = dsl.buildFunctions()
            if (funcs.isNotEmpty()) fs.functions(funcs)
            dsl.maxBoost?.let { fs.maxBoost(it) }
            dsl.minScore?.let { fs.minScore(it) }
            dsl.scoreMode?.let { fs.scoreMode(it) }
            dsl.boostMode?.let { fs.boostMode(it) }
            dsl.boost?.let { fs.boost(it) }
            dsl._name?.let { fs.queryName(it) }
            fs
        }
    }
    this.addQuery(fsQuery)
    return this
}

