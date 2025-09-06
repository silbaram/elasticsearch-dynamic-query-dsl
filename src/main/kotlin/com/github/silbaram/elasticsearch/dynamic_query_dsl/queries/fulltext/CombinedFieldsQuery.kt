package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.CombinedFieldsOperator
import co.elastic.clients.elasticsearch._types.query_dsl.CombinedFieldsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

/**
 * combined_fields 쿼리 DSL 빌더
 * - 하나의 질의어를 여러 필드의 용어 사전을 결합해 검색
 * - query가 null/blank이거나 fields가 비면 생성하지 않음
 */
fun Query.Builder.combinedFields(
    query: String?,
    fields: List<String>,
    operator: CombinedFieldsOperator? = null,
    minimumShouldMatch: String? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    if (query.isNullOrEmpty() || fields.isEmpty()) return this

    return this.combinedFields { b ->
        b.query(query)
            .fields(fields)
        operator?.let { b.operator(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        autoGenerateSynonymsPhraseQuery?.let { b.autoGenerateSynonymsPhraseQuery(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b
    }
}

fun combinedFieldsQuery(
    query: String?,
    fields: List<String>,
    operator: CombinedFieldsOperator? = null,
    minimumShouldMatch: String? = null,
    autoGenerateSynonymsPhraseQuery: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (query.isNullOrEmpty() || fields.isEmpty()) {
        null
    } else {
        val b = CombinedFieldsQuery.Builder()
            .query(query)
            .fields(fields)
        operator?.let { b.operator(it) }
        minimumShouldMatch?.let { b.minimumShouldMatch(it) }
        autoGenerateSynonymsPhraseQuery?.let { b.autoGenerateSynonymsPhraseQuery(it) }
        boost?.let { b.boost(it) }
        _name?.let { b.queryName(it) }
        b.build()._toQuery()
    }
}
