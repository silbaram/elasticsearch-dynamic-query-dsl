package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.util.ObjectBuilder

class MatchQueryDsl {
    var field: String? = null
    var query: String? = null
    var analyzer: String? = null
    var operator: Operator? = null
    var minimumShouldMatch: String? = null
    var fuzziness: String? = null
    var prefixLength: Int? = null
    var maxExpansions: Int? = null
    var fuzzyTranspositions: Boolean? = null
    var fuzzyRewrite: String? = null
    var autoGenerateSynonymsPhraseQuery: Boolean? = null
    var lenient: Boolean? = null
    var zeroTermsQuery: ZeroTermsQuery? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.matchQuery(fn: MatchQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = MatchQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val q = dsl.query?.takeIf { it.isNotBlank() } ?: return this

    return this.match { m ->
        m.field(f)
        m.query(q)
        dsl.analyzer?.let { m.analyzer(it) }
        dsl.operator?.let { m.operator(it) }
        dsl.minimumShouldMatch?.let { m.minimumShouldMatch(it) }
        dsl.fuzziness?.let { m.fuzziness(it) }
        dsl.prefixLength?.let { m.prefixLength(it) }
        dsl.maxExpansions?.let { m.maxExpansions(it) }
        dsl.fuzzyTranspositions?.let { m.fuzzyTranspositions(it) }
        dsl.fuzzyRewrite?.let { m.fuzzyRewrite(it) }
        dsl.autoGenerateSynonymsPhraseQuery?.let { m.autoGenerateSynonymsPhraseQuery(it) }
        dsl.lenient?.let { m.lenient(it) }
        dsl.zeroTermsQuery?.let { m.zeroTermsQuery(it) }
        dsl.boost?.let { m.boost(it) }
        dsl._name?.let { m.queryName(it) }
        m
    }
}

