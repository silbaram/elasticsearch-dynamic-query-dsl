package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhrasePrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.util.ObjectBuilder

/**
 * match_phrase_prefix 쿼리 DSL 빌더
 * - 마지막 토큰만 접두어 확장하며 구문 순서는 유지
 * - query가 null/blank면 생성하지 않음
 */
fun Query.Builder.matchPhrasePrefix(
    field: String,
    query: String?,
    analyzer: String? = null,
    slop: Int? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    maxExpansions: Int? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty()) return this

    return this.matchPhrasePrefix { b ->
        b.field(field)
            .query(query)
        analyzer?.let { b.analyzer(it) }
        slop?.let { b.slop(it) }
        zeroTermsQuery?.let { b.zeroTermsQuery(it) }
        maxExpansions?.let { b.maxExpansions(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}

fun matchPhrasePrefixQuery(
    field: String,
    query: String?,
    analyzer: String? = null,
    slop: Int? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    maxExpansions: Int? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (query.isNullOrEmpty()) {
        null
    } else {
        val b = MatchPhrasePrefixQuery.Builder()
            .field(field)
            .query(query)
        analyzer?.let { b.analyzer(it) }
        slop?.let { b.slop(it) }
        zeroTermsQuery?.let { b.zeroTermsQuery(it) }
        maxExpansions?.let { b.maxExpansions(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b.build()._toQuery()
    }
}

