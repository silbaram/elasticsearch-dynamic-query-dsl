package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.json.JsonData
import co.elastic.clients.util.ObjectBuilder

class PercolateQueryDsl {
    var field: String? = null
    var document: Map<String, Any?>? = null
    var documents: List<Map<String, Any?>> = emptyList()
    var index: String? = null
    var id: String? = null
    var routing: String? = null
    var preference: String? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.percolateQuery(fn: PercolateQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = PercolateQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this

    // Determine input source: single document, multiple documents, or index/id
    val singleDoc = dsl.document?.filterKeys { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
    val docs = dsl.documents.mapNotNull { m -> m.filterKeys { it.isNotBlank() }.takeIf { it.isNotEmpty() } }
    val hasDocs = singleDoc != null || docs.isNotEmpty()
    val hasIndexId = !dsl.index.isNullOrBlank() && !dsl.id.isNullOrBlank()

    if (!hasDocs && !hasIndexId) return this

    return this.percolate { p ->
        p.field(field)
        if (singleDoc != null) {
            p.document(JsonData.of(singleDoc))
        }
        if (docs.isNotEmpty()) {
            p.documents(docs.map { JsonData.of(it) })
        }
        if (hasIndexId) {
            p.index(dsl.index)
            p.id(dsl.id)
            dsl.routing?.takeIf { it.isNotBlank() }?.let { p.routing(it) }
            dsl.preference?.takeIf { it.isNotBlank() }?.let { p.preference(it) }
        }
        dsl.boost?.let { p.boost(it) }
        dsl._name?.let { p.queryName(it) }
        p
    }
}

