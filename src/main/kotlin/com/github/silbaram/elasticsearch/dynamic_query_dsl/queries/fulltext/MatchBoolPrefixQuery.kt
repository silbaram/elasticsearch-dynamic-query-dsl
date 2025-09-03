package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MatchBoolPrefixQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

/**
 * Top-level builder extension for match_bool_prefix query.
 */
fun Query.Builder.matchBoolPrefix(
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
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty()) {
        // No-op: returns builder unchanged (callers should avoid empty input at top level)
        return this
    }

    return this.matchBoolPrefix { b ->
        b.field(field)
            .query(query)
        analyzer?.let { b.analyzer(it) }
        operator?.let { b.operator(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        fuzziness?.let { b.fuzziness(it) }
        prefixLength?.let { b.prefixLength(it) }
        maxExpansions?.let { b.maxExpansions(it) }
        fuzzyTranspositions?.let { b.fuzzyTranspositions(it) }
        fuzzyRewrite?.let { b.fuzzyRewrite(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}

fun matchBoolPrefixQuery(
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
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (query.isNullOrEmpty()) {
        null
    } else {
        val builder = MatchBoolPrefixQuery.Builder()
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
        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        builder.build()._toQuery()
    }
}
