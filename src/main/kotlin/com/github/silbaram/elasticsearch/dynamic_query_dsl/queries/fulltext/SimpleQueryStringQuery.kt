package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.SimpleQueryStringQuery
import co.elastic.clients.util.ObjectBuilder
import co.elastic.clients.elasticsearch._types.query_dsl.SimpleQueryStringFlag

/**
 * simple_query_string 쿼리 DSL 빌더
 * - 안전한 Lucene-like 문법(오류 허용)으로 다중 필드 질의
 * - query가 null/blank이면 생성하지 않음
 */
fun Query.Builder.simpleQueryString(
    query: String?,
    fields: List<String> = emptyList(),
    defaultOperator: Operator? = null,
    analyzer: String? = null,
    quoteFieldSuffix: String? = null,
    analyzeWildcard: Boolean? = null,
    flags: List<SimpleQueryStringFlag> = emptyList(),
    fuzzyMaxExpansions: Int? = null,
    fuzzyPrefixLength: Int? = null,
    fuzzyTranspositions: Boolean? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    minimumShouldMatch: String? = null,
    lenient: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty()) return this

    return this.simpleQueryString { b ->
        b.query(query)
        if (fields.isNotEmpty()) b.fields(fields)
        defaultOperator?.let { b.defaultOperator(it) }
        analyzer?.let { b.analyzer(it) }
        quoteFieldSuffix?.let { b.quoteFieldSuffix(it) }
        analyzeWildcard?.let { b.analyzeWildcard(it) }
        if (flags.isNotEmpty()) b.flags(flags)
        fuzzyMaxExpansions?.let { b.fuzzyMaxExpansions(it) }
        fuzzyPrefixLength?.let { b.fuzzyPrefixLength(it) }
        fuzzyTranspositions?.let { b.fuzzyTranspositions(it) }
        autoGenerateSynonymsPhraseQuery?.let { b.autoGenerateSynonymsPhraseQuery(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        lenient?.let { b.lenient(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}

fun simpleQueryStringQuery(
    query: String?,
    fields: List<String> = emptyList(),
    defaultOperator: Operator? = null,
    analyzer: String? = null,
    quoteFieldSuffix: String? = null,
    analyzeWildcard: Boolean? = null,
    flags: List<SimpleQueryStringFlag> = emptyList(),
    fuzzyMaxExpansions: Int? = null,
    fuzzyPrefixLength: Int? = null,
    fuzzyTranspositions: Boolean? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    minimumShouldMatch: String? = null,
    lenient: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (query.isNullOrEmpty()) {
        null
    } else {
        val b = SimpleQueryStringQuery.Builder()
            .query(query)
        if (fields.isNotEmpty()) b.fields(fields)
        defaultOperator?.let { b.defaultOperator(it) }
        analyzer?.let { b.analyzer(it) }
        quoteFieldSuffix?.let { b.quoteFieldSuffix(it) }
        analyzeWildcard?.let { b.analyzeWildcard(it) }
        if (flags.isNotEmpty()) b.flags(flags)
        fuzzyMaxExpansions?.let { b.fuzzyMaxExpansions(it) }
        fuzzyPrefixLength?.let { b.fuzzyPrefixLength(it) }
        fuzzyTranspositions?.let { b.fuzzyTranspositions(it) }
        autoGenerateSynonymsPhraseQuery?.let { b.autoGenerateSynonymsPhraseQuery(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        lenient?.let { b.lenient(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b.build()._toQuery()
    }
}
