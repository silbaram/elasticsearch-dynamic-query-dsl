package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.util.ObjectBuilder

/**
 * match_phrase 쿼리 DSL 빌더
 * - 입력 문자열이 null/blank이면 쿼리를 생성하지 않음(null 반환 또는 no-op)
 * - 옵션: analyzer, slop, zeroTermsQuery, boost, _name
 */
fun Query.Builder.matchPhrase(
    field: String,
    query: String?,
    analyzer: String? = null,
    slop: Int? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty()) {
        return this
    }

    return this.matchPhrase { b ->
        b.field(field)
            .query(query)
        analyzer?.let { b.analyzer(it) }
        slop?.let { b.slop(it) }
        zeroTermsQuery?.let { b.zeroTermsQuery(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}

fun matchPhraseQuery(
    field: String,
    query: String?,
    analyzer: String? = null,
    slop: Int? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (query.isNullOrEmpty()) {
        null
    } else {
        val builder = MatchPhraseQuery.Builder()
            .field(field)
            .query(query)

        analyzer?.let { builder.analyzer(it) }
        slop?.let { builder.slop(it) }
        zeroTermsQuery?.let { builder.zeroTermsQuery(it) }
        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        builder.build()._toQuery()
    }
}

