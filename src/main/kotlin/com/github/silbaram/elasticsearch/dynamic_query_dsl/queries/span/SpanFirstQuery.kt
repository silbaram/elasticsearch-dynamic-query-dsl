package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.SpanQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanFirstQuery as EsSpanFirstQuery

class SpanFirstQueryBuilder {
    var match: Query? = null
    var end: Int? = null
    var boost: Float? = null
    var _name: String? = null

    internal fun build(): Query? {
        val nonNullMatch = match ?: return null
        val nonNullEnd = end ?: throw IllegalArgumentException("The 'end' parameter is required for a span_first query.")

        val spanQueryVariant: SpanQuery = when (nonNullMatch._kind()) {
            Query.Kind.SpanTerm -> SpanQuery.Builder().spanTerm(nonNullMatch.spanTerm()).build()
            Query.Kind.SpanNear -> SpanQuery.Builder().spanNear(nonNullMatch.spanNear()).build()
            Query.Kind.SpanOr -> SpanQuery.Builder().spanOr(nonNullMatch.spanOr()).build()
            Query.Kind.SpanContaining -> SpanQuery.Builder().spanContaining(nonNullMatch.spanContaining()).build()
            Query.Kind.SpanWithin -> SpanQuery.Builder().spanWithin(nonNullMatch.spanWithin()).build()
            Query.Kind.SpanNot -> SpanQuery.Builder().spanNot(nonNullMatch.spanNot()).build()
            Query.Kind.SpanMulti -> SpanQuery.Builder().spanMulti(nonNullMatch.spanMulti()).build()
            Query.Kind.SpanFirst -> SpanQuery.Builder().spanFirst(nonNullMatch.spanFirst()).build()
            Query.Kind.SpanFieldMasking -> SpanQuery.Builder().spanFieldMasking(nonNullMatch.spanFieldMasking()).build()
            else -> throw IllegalArgumentException("The provided 'match' query of kind '${nonNullMatch._kind()}' is not a valid span query type.")
        }

        return Query.of { q -> 
            q.spanFirst(
                EsSpanFirstQuery.Builder()
                    .match(spanQueryVariant)
                    .end(nonNullEnd)
                    .boost(boost)
                    .queryName(_name)
                    .build()
            )
        }
    }
}

fun spanFirstQuery(block: SpanFirstQueryBuilder.() -> Unit): Query? {
    return SpanFirstQueryBuilder().apply(block).build()
}
