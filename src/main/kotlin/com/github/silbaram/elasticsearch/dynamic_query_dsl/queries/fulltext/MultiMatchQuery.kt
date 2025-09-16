package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.util.ObjectBuilder

/**
 * multi_match 쿼리 DSL 빌더 (일반형)
 * - 여러 필드에 단일 질의어를 적용
 * - type(best_fields, most_fields, cross_fields, phrase, phrase_prefix, bool_prefix) 지원
 * - query가 null/blank이거나 fields가 비면 생성하지 않음
 */
fun Query.Builder.multiMatch(
    query: String?,
    fields: List<String>,
    type: TextQueryType? = null,
    operator: Operator? = null,
    minimumShouldMatch: String? = null,
    analyzer: String? = null,
    slop: Int? = null,
    tieBreaker: Double? = null,
    fuzziness: String? = null,
    prefixLength: Int? = null,
    maxExpansions: Int? = null,
    fuzzyTranspositions: Boolean? = null,
    fuzzyRewrite: String? = null,
    lenient: Boolean? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty() || fields.isEmpty()) return this

    return this.multiMatch { b ->
        b.query(query).fields(fields)
        type?.let { b.type(it) }
        operator?.let { b.operator(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        analyzer?.let { b.analyzer(it) }
        slop?.let { b.slop(it) }
        tieBreaker?.let { b.tieBreaker(it) }
        fuzziness?.let { b.fuzziness(it) }
        prefixLength?.let { b.prefixLength(it) }
        maxExpansions?.let { b.maxExpansions(it) }
        fuzzyTranspositions?.let { b.fuzzyTranspositions(it) }
        fuzzyRewrite?.let { b.fuzzyRewrite(it) }
        lenient?.let { b.lenient(it) }
        zeroTermsQuery?.let { b.zeroTermsQuery(it) }
        autoGenerateSynonymsPhraseQuery?.let { b.autoGenerateSynonymsPhraseQuery(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}
