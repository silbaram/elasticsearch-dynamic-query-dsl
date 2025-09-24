package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.joining

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.search.InnerHits
import co.elastic.clients.util.ObjectBuilder
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

class NestedQueryDsl {
    var path: String? = null
    var ignoreUnmapped: Boolean? = null
    var scoreMode: ChildScoreMode? = null
    var boost: Float? = null
    var _name: String? = null

    private var queryBuilder: (Query.Builder.() -> Unit)? = null
    private var queryObject: Query? = null
    private var innerHitsBuilder: (InnerHits.Builder.() -> Unit)? = null
    private var innerHitsObject: InnerHits? = null

    fun query(fn: Query.Builder.() -> Unit) {
        queryBuilder = fn
        queryObject = null
    }

    fun query(query: Query) {
        queryObject = query
        queryBuilder = null
    }

    fun innerHits(fn: InnerHits.Builder.() -> Unit) {
        innerHitsBuilder = fn
        innerHitsObject = null
    }

    fun innerHits(innerHits: InnerHits) {
        innerHitsObject = innerHits
        innerHitsBuilder = null
    }

    internal fun buildQuery(): Query? = queryObject ?: queryBuilder?.let { queryOrNull(it) }

    internal fun buildInnerHits(): InnerHits? = innerHitsObject ?: innerHitsBuilder?.let { builder ->
        InnerHits.Builder().apply(builder).build()
    }
}

fun Query.Builder.nestedQuery(fn: NestedQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = NestedQueryDsl().apply(fn)
    val path = dsl.path?.takeIf { it.isNotBlank() } ?: return this
    val innerQuery = dsl.buildQuery() ?: return this
    val innerHits = dsl.buildInnerHits()

    return this.nested { n ->
        n.path(path)
        n.query(innerQuery)
        dsl.ignoreUnmapped?.let { n.ignoreUnmapped(it) }
        dsl.scoreMode?.let { n.scoreMode(it) }
        innerHits?.let { n.innerHits(it) }
        dsl.boost?.let { n.boost(it) }
        dsl._name?.let { n.queryName(it) }
        n
    }
}

class HasChildQueryDsl {
    var type: String? = null
    var ignoreUnmapped: Boolean? = null
    var minChildren: Int? = null
    var maxChildren: Int? = null
    var scoreMode: ChildScoreMode? = null
    var boost: Float? = null
    var _name: String? = null

    private var queryBuilder: (Query.Builder.() -> Unit)? = null
    private var queryObject: Query? = null
    private var innerHitsBuilder: (InnerHits.Builder.() -> Unit)? = null
    private var innerHitsObject: InnerHits? = null

    fun query(fn: Query.Builder.() -> Unit) {
        queryBuilder = fn
        queryObject = null
    }

    fun query(query: Query) {
        queryObject = query
        queryBuilder = null
    }

    fun innerHits(fn: InnerHits.Builder.() -> Unit) {
        innerHitsBuilder = fn
        innerHitsObject = null
    }

    fun innerHits(innerHits: InnerHits) {
        innerHitsObject = innerHits
        innerHitsBuilder = null
    }

    internal fun buildQuery(): Query? = queryObject ?: queryBuilder?.let { queryOrNull(it) }

    internal fun buildInnerHits(): InnerHits? = innerHitsObject ?: innerHitsBuilder?.let { builder ->
        InnerHits.Builder().apply(builder).build()
    }
}

fun Query.Builder.hasChildQuery(fn: HasChildQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = HasChildQueryDsl().apply(fn)
    val type = dsl.type?.takeIf { it.isNotBlank() } ?: return this
    val childQuery = dsl.buildQuery() ?: return this
    val innerHits = dsl.buildInnerHits()

    return this.hasChild { hc ->
        hc.type(type)
        hc.query(childQuery)
        dsl.ignoreUnmapped?.let { hc.ignoreUnmapped(it) }
        dsl.minChildren?.let { hc.minChildren(it) }
        dsl.maxChildren?.let { hc.maxChildren(it) }
        dsl.scoreMode?.let { hc.scoreMode(it) }
        innerHits?.let { hc.innerHits(it) }
        dsl.boost?.let { hc.boost(it) }
        dsl._name?.let { hc.queryName(it) }
        hc
    }
}

class HasParentQueryDsl {
    var parentType: String? = null
    var ignoreUnmapped: Boolean? = null
    var score: Boolean? = null
    var boost: Float? = null
    var _name: String? = null

    private var queryBuilder: (Query.Builder.() -> Unit)? = null
    private var queryObject: Query? = null
    private var innerHitsBuilder: (InnerHits.Builder.() -> Unit)? = null
    private var innerHitsObject: InnerHits? = null

    fun query(fn: Query.Builder.() -> Unit) {
        queryBuilder = fn
        queryObject = null
    }

    fun query(query: Query) {
        queryObject = query
        queryBuilder = null
    }

    fun innerHits(fn: InnerHits.Builder.() -> Unit) {
        innerHitsBuilder = fn
        innerHitsObject = null
    }

    fun innerHits(innerHits: InnerHits) {
        innerHitsObject = innerHits
        innerHitsBuilder = null
    }

    internal fun buildQuery(): Query? = queryObject ?: queryBuilder?.let { queryOrNull(it) }

    internal fun buildInnerHits(): InnerHits? = innerHitsObject ?: innerHitsBuilder?.let { builder ->
        InnerHits.Builder().apply(builder).build()
    }
}

fun Query.Builder.hasParentQuery(fn: HasParentQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = HasParentQueryDsl().apply(fn)
    val parentType = dsl.parentType?.takeIf { it.isNotBlank() } ?: return this
    val parentQuery = dsl.buildQuery() ?: return this
    val innerHits = dsl.buildInnerHits()

    return this.hasParent { hp ->
        hp.parentType(parentType)
        hp.query(parentQuery)
        dsl.ignoreUnmapped?.let { hp.ignoreUnmapped(it) }
        dsl.score?.let { hp.score(it) }
        innerHits?.let { hp.innerHits(it) }
        dsl.boost?.let { hp.boost(it) }
        dsl._name?.let { hp.queryName(it) }
        hp
    }
}

class ParentIdQueryDsl {
    var id: String? = null
    var type: String? = null
    var ignoreUnmapped: Boolean? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.parentIdQuery(fn: ParentIdQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = ParentIdQueryDsl().apply(fn)
    val id = dsl.id?.takeIf { it.isNotBlank() } ?: return this
    val type = dsl.type?.takeIf { it.isNotBlank() }

    return this.parentId { pi ->
        pi.id(id)
        type?.let { pi.type(it) }
        dsl.ignoreUnmapped?.let { pi.ignoreUnmapped(it) }
        dsl.boost?.let { pi.boost(it) }
        dsl._name?.let { pi.queryName(it) }
        pi
    }
}
