package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

class PinnedQueryDsl {
    var boost: Float? = null
    var _name: String? = null
    var organic: Query? = null

    private val idBacking = mutableListOf<String>()

    var ids: List<String?>?
        get() = idBacking.toList()
        set(value) {
            idBacking.clear()
            value?.forEach { addId(it) }
        }

    fun ids(vararg value: String?) {
        value.forEach { addId(it) }
    }

    fun addId(id: String?) {
        val trimmed = id?.trim()
        if (!trimmed.isNullOrEmpty()) {
            idBacking.add(trimmed)
        }
    }

    fun organic(fn: Query.Builder.() -> Unit) {
        organic = queryOrNull(fn)
    }

    internal fun resolvedIds(): List<String> = idBacking.filter { it.isNotEmpty() }
}

fun Query.Builder.pinnedQuery(fn: PinnedQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = PinnedQueryDsl().apply(fn)
    val ids = dsl.resolvedIds()
    val organic = dsl.organic ?: return this
    if (ids.isEmpty()) return this

    return this.pinned { pinned ->
        pinned.ids(ids)
        pinned.organic(organic)
        dsl.boost?.let { pinned.boost(it) }
        dsl._name?.let { pinned.queryName(it) }
        pinned
    }
}
