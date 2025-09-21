package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

/** DSL helper for building script_score queries */
class ScriptScoreQueryDsl : ScriptOptionsDsl() {
    var boost: Float? = null
    var _name: String? = null
    var minScore: Float? = null
    var query: Query? = null

    fun query(fn: Query.Builder.() -> Unit) {
        query = queryOrNull(fn)
    }
}

fun Query.Builder.scriptScoreQuery(fn: ScriptScoreQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = ScriptScoreQueryDsl().apply(fn)
    val script = dsl.buildScript() ?: return this

    val innerQuery = dsl.query ?: Query.of { q ->
        q.matchAll { matchAll -> matchAll }
    }

    return this.scriptScore { builder ->
        builder.query(innerQuery)
        builder.script(script)
        dsl.boost?.let { builder.boost(it) }
        dsl._name?.let { builder.queryName(it) }
        dsl.minScore?.let { builder.minScore(it) }
        builder
    }
}
