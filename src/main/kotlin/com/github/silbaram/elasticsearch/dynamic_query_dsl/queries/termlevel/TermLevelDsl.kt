package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import co.elastic.clients.util.ObjectBuilder
import co.elastic.clients.json.JsonData

// --- Term Query DSL ---
class TermQueryDsl {
    var field: String? = null
    var value: String? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.termQuery(fn: TermQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = TermQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val v = dsl.value?.takeIf { it.isNotBlank() } ?: return this
    return this.term { t ->
        t.field(f)
        t.value(v)
        dsl.boost?.let { t.boost(it) }
        dsl._name?.let { t.queryName(it) }
        t
    }
}

// --- Exists Query DSL ---
class ExistsQueryDsl {
    var field: String? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.existsQuery(fn: ExistsQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = ExistsQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    return this.exists { e ->
        e.field(f)
        dsl.boost?.let { e.boost(it) }
        dsl._name?.let { e.queryName(it) }
        e
    }
}

// --- Terms Query DSL ---
class TermsQueryDsl {
    var field: String? = null
    var values: List<String?>? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.termsQuery(fn: TermsQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = TermsQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val vs = dsl.values?.mapNotNull { it }?.takeIf { it.isNotEmpty() } ?: return this
    return this.terms { tq ->
        tq.field(f)
        tq.terms(
            TermsQueryField.Builder().value(vs.map { FieldValue.of(it) }).build()
        )
        dsl.boost?.let { tq.boost(it) }
        dsl._name?.let { tq.queryName(it) }
        tq
    }
}

// --- Range Query DSL ---
class RangeQueryDsl {
    var field: String? = null
    var from: String? = null
    var to: String? = null
    var gt: Any? = null
    var lt: Any? = null
    var gte: Any? = null
    var lte: Any? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.rangeQuery(fn: RangeQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = RangeQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val hasBounds = !(dsl.from.isNullOrEmpty() && dsl.to.isNullOrEmpty() && dsl.gt == null && dsl.lt == null && dsl.gte == null && dsl.lte == null)
    if (!hasBounds) return this
    return this.range { r ->
        r.field(f)
        dsl.from?.let { r.from(it) }
        dsl.to?.let { r.to(it) }
        dsl.gt?.let { r.gt(jsonDataConvert(it)) }
        dsl.lt?.let { r.lt(jsonDataConvert(it)) }
        dsl.gte?.let { r.gte(jsonDataConvert(it)) }
        dsl.lte?.let { r.lte(jsonDataConvert(it)) }
        dsl.boost?.let { r.boost(it) }
        dsl._name?.let { r.queryName(it) }
        r
    }
}

// --- Match All DSL ---
class MatchAllDsl {
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.matchAllDsl(fn: MatchAllDsl.() -> Unit = {}): ObjectBuilder<Query> {
    val dsl = MatchAllDsl().apply(fn)
    return this.matchAll { m ->
        dsl.boost?.let { m.boost(it) }
        dsl._name?.let { m.queryName(it) }
        m
    }
}

// Internal helper used by range builders
fun jsonDataConvert(value: Any?): JsonData? = value?.let { JsonData.of(it) }
