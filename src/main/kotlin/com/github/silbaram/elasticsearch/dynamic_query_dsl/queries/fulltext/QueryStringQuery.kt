package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery
import co.elastic.clients.util.ObjectBuilder

/**
 * query_string 쿼리 DSL 빌더
 * - Lucene query syntax로 다중 필드 질의를 구성
 * - query가 null/blank이면 생성하지 않음
 */
fun Query.Builder.queryString(
    query: String?,
    fields: List<String> = emptyList(),
    defaultField: String? = null,
    analyzer: String? = null,
    quoteAnalyzer: String? = null,
    quoteFieldSuffix: String? = null,
    defaultOperator: Operator? = null,
    allowLeadingWildcard: Boolean? = null,
    analyzeWildcard: Boolean? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    enablePositionIncrements: Boolean? = null,
    fuzziness: String? = null,
    fuzzyMaxExpansions: Int? = null,
    fuzzyPrefixLength: Int? = null,
    fuzzyTranspositions: Boolean? = null,
    lenient: Boolean? = null,
    minimumShouldMatch: String? = null,
    phraseSlop: Double? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty()) return this

    return this.queryString { b ->
        b.query(query)
        if (fields.isNotEmpty()) b.fields(fields)
        defaultField?.let { b.defaultField(it) }
        analyzer?.let { b.analyzer(it) }
        quoteAnalyzer?.let { b.quoteAnalyzer(it) }
        quoteFieldSuffix?.let { b.quoteFieldSuffix(it) }
        defaultOperator?.let { b.defaultOperator(it) }
        allowLeadingWildcard?.let { b.allowLeadingWildcard(it) }
        analyzeWildcard?.let { b.analyzeWildcard(it) }
        autoGenerateSynonymsPhraseQuery?.let { b.autoGenerateSynonymsPhraseQuery(it) }
        enablePositionIncrements?.let { b.enablePositionIncrements(it) }
        fuzziness?.let { b.fuzziness(it) }
        fuzzyMaxExpansions?.let { b.fuzzyMaxExpansions(it) }
        fuzzyPrefixLength?.let { b.fuzzyPrefixLength(it) }
        fuzzyTranspositions?.let { b.fuzzyTranspositions(it) }
        lenient?.let { b.lenient(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        phraseSlop?.let { b.phraseSlop(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}
