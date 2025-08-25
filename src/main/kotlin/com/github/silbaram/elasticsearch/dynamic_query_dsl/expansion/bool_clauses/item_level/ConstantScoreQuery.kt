package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.ConstantScoreQueryDsl
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

fun SubQueryBuilders.constantScoreQuery(
    boost: Float? = null,
    _name: String? = null,
    fn: ConstantScoreQueryDsl.() -> Unit,
): Query? {
    val dsl = ConstantScoreQueryDsl().apply(fn)
    val filterQuery = dsl.buildFilterQuery() ?: return null

    val constantScoreQuery = Query.of { q ->
        q.constantScore { cs ->
            cs.filter(filterQuery)
            boost?.let { cs.boost(it) }
            _name?.let { cs.queryName(it) }
            cs
        }
    }

    this.addQuery(constantScoreQuery)
    return null
}

