package com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.integrations

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.ConstantScoreQueryDsl
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders

fun SubQueryBuilders.constantScoreQuery(
    fn: ConstantScoreQueryDsl.() -> Unit,
): SubQueryBuilders {
    val dsl = ConstantScoreQueryDsl().apply(fn)
    val filterQuery = dsl.buildFilterQuery() ?: return this

    val constantScoreQuery = Query.of { q ->
        q.constantScore { cs ->
            cs.filter(filterQuery)
            dsl.boost?.let { cs.boost(it) }
            dsl._name?.let { cs.queryName(it) }
            cs
        }
    }

    this.addQuery(constantScoreQuery)
    return this
}
