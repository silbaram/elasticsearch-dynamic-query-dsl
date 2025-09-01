package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery

fun matchQuery(
    field: String,
    query: String?,
    analyzer: String? = null,
    operator: Operator? = null,
    minimumShouldMatch: String? = null,
    fuzziness: String? = null,
    prefixLength: Int? = null,
    maxExpansions: Int? = null,
    fuzzyTranspositions: Boolean? = null,
    fuzzyRewrite: String? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    lenient: Boolean? = null,
    zeroTermsQuery: ZeroTermsQuery? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (query.isNullOrEmpty()) {
        null
    } else {
        val builder = MatchQuery.Builder()
            .field(field)
            .query(query)

        analyzer?.let { builder.analyzer(it) }
        operator?.let { builder.operator(it) }
        minimumShouldMatch?.let { builder.minimumShouldMatch(it) }
        fuzziness?.let { builder.fuzziness(it) }
        prefixLength?.let { builder.prefixLength(it) }
        maxExpansions?.let { builder.maxExpansions(it) }
        fuzzyTranspositions?.let { builder.fuzzyTranspositions(it) }
        fuzzyRewrite?.let { builder.fuzzyRewrite(it) }
        autoGenerateSynonymsPhraseQuery?.let { builder.autoGenerateSynonymsPhraseQuery(it) }
        lenient?.let { builder.lenient(it) }
        zeroTermsQuery?.let { builder.zeroTermsQuery(it) }
        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        builder.build()._toQuery()
    }
}
