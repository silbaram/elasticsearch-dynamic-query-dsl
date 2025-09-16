package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.util.ObjectBuilder

/**
 * multi_match (type=phrase) DSL 빌더
 * - 단일 질의어를 여러 필드에 구문(phrase) 의미로 적용
 * - query가 null/blank이거나 fields가 비면 생성하지 않음
 */
fun Query.Builder.multiMatchPhrase(
    query: String?,
    fields: List<String>,
    analyzer: String? = null,
    slop: Int? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty() || fields.isEmpty()) return this

    return this.multiMatch { b ->
        b.query(query)
            .fields(fields)
            .type(TextQueryType.Phrase)
        analyzer?.let { b.analyzer(it) }
        slop?.let { b.slop(it) }
        zeroTermsQuery?.let { b.zeroTermsQuery(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}
