package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.SpanContainingQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanNearQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanTermQuery

/**
 * Converts a generic Query object to a specific SpanQuery object, if it is a span query variant.
 * This is necessary because span container queries (like span_containing) require SpanQuery inputs,
 * but for DSL consistency, all query builder functions should return the generic Query type.
 */
private fun Query.toSpanQuery(): SpanQuery? {
    return when (this._kind()) {
        Query.Kind.SpanTerm -> SpanQuery.of { s -> s.spanTerm(this.spanTerm()) }
        Query.Kind.SpanNear -> SpanQuery.of { s -> s.spanNear(this.spanNear()) }
        Query.Kind.SpanContaining -> SpanQuery.of { s -> s.spanContaining(this.spanContaining()) }
        Query.Kind.SpanFirst -> SpanQuery.of { s -> s.spanFirst(this.spanFirst()) }
        Query.Kind.SpanNot -> SpanQuery.of { s -> s.spanNot(this.spanNot()) }
        Query.Kind.SpanOr -> SpanQuery.of { s -> s.spanOr(this.spanOr()) }
        Query.Kind.SpanWithin -> SpanQuery.of { s -> s.spanWithin(this.spanWithin()) }
        Query.Kind.SpanMulti -> SpanQuery.of { s -> s.spanMulti(this.spanMulti()) }
        // Note: span_field_masking is not a direct query kind, it's a parameter within other span queries.
        else -> null
    }
}

/**
 * Build a span_term as Query
 */
fun spanTermQuery(
    field: String,
    value: String?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    if (field.isBlank() || value.isNullOrBlank()) return null

    val builder = SpanTermQuery.Builder()
        .field(field)
        .value(value)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_near as Query
 */
fun spanNearQuery(
    clauses: List<Query?>?,
    slop: Int,
    inOrder: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val validSpanClauses = clauses?.mapNotNull { it?.toSpanQuery() }.orEmpty()
    if (validSpanClauses.isEmpty() || slop < 0) return null

    val builder = SpanNearQuery.Builder()
        .slop(slop)
        .clauses(validSpanClauses)

    inOrder?.let { builder.inOrder(it) }
    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_containing top-level Query from two Query inputs.
 * The inputs must be variants of span queries.
 * Returns null when either input is null or not a valid span query.
 */
fun spanContainingQuery(
    little: Query?,
    big: Query?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val littleSpan = little?.toSpanQuery()
    val bigSpan = big?.toSpanQuery()

    if (littleSpan == null || bigSpan == null) return null

    val builder = SpanContainingQuery.Builder()
        .little(littleSpan)
        .big(bigSpan)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

