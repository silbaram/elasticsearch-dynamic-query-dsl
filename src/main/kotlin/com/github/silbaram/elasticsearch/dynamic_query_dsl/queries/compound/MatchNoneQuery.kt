package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

class MatchNoneDsl {
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.matchNoneQuery(fn: MatchNoneDsl.() -> Unit = {}): ObjectBuilder<Query> {
    val dsl = MatchNoneDsl().apply(fn)
    return this.matchNone { mn ->
        dsl.boost?.let { mn.boost(it) }
        dsl._name?.let { mn.queryName(it) }
        mn
    }
}

