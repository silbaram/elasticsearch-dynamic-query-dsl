package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField

fun termsQuery(
    field: String,
    values: List<String?>?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val filterValues = values?.mapNotNull { it }
    return if (filterValues?.isNotEmpty() == true) {
        TermsQuery.Builder()
        .field(field)
        .terms(
            TermsQueryField.Builder().value(
                filterValues.asSequence().map {
                    FieldValue.of(it)
                }.toList()
            ).build()
        )
        .boost(boost)
        .queryName(_name)
        .build()._toQuery()
    } else {
        null
    }
}