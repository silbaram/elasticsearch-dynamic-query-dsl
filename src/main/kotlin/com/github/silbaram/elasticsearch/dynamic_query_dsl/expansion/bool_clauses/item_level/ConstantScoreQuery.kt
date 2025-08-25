package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

fun SubQueryBuilders.constantScoreQuery(
    boost: Float? = null,
    _name: String? = null,
    fn: SubQueryBuilders.() -> Any?
): Query? {
    val subQuery = SubQueryBuilders()
    val result = subQuery.fn()
    if (result is Query) {
        subQuery.addQuery(result)
    }

    val filterQuery: Query? = when (subQuery.size()) {
        0 -> null
        1 -> {
            var temp: Query? = null
            subQuery.forEach { temp = it }
            temp
        }
        else -> Query.of { q ->
            q.bool { b ->
                subQuery.forEach { b.must(it) }
                b
            }
        }
    }

    if (filterQuery == null) {
        return null
    }

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
