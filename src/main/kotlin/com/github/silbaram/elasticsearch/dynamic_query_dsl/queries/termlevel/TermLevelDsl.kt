package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.Script
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

// --- Prefix Query DSL ---
class PrefixQueryDsl {
    var field: String? = null
    var value: String? = null
    var rewrite: String? = null
    var caseInsensitive: Boolean? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.prefixQuery(fn: PrefixQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = PrefixQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val v = dsl.value?.takeIf { it.isNotBlank() } ?: return this
    return this.prefix { p ->
        p.field(f)
        p.value(v)
        dsl.rewrite?.let { p.rewrite(it) }
        dsl.caseInsensitive?.let { p.caseInsensitive(it) }
        dsl.boost?.let { p.boost(it) }
        dsl._name?.let { p.queryName(it) }
        p
    }
}

// --- Wildcard Query DSL ---
class WildcardQueryDsl {
    var field: String? = null
    var value: String? = null
    var caseInsensitive: Boolean? = null
    var rewrite: String? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.wildcardQuery(fn: WildcardQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = WildcardQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val v = dsl.value?.takeIf { it.isNotBlank() } ?: return this
    return this.wildcard { w ->
        w.field(f)
        w.value(v)
        dsl.caseInsensitive?.let { w.caseInsensitive(it) }
        dsl.rewrite?.let { w.rewrite(it) }
        dsl.boost?.let { w.boost(it) }
        dsl._name?.let { w.queryName(it) }
        w
    }
}

// --- Regexp Query DSL ---
class RegexpQueryDsl {
    var field: String? = null
    var value: String? = null
    var flags: String? = null
    var maxDeterminizedStates: Int? = null
    var rewrite: String? = null
    var caseInsensitive: Boolean? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.regexpQuery(fn: RegexpQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = RegexpQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val v = dsl.value?.takeIf { it.isNotBlank() } ?: return this
    return this.regexp { r ->
        r.field(f)
        r.value(v)
        dsl.flags?.let { r.flags(it) }
        dsl.maxDeterminizedStates?.let { r.maxDeterminizedStates(it) }
        dsl.rewrite?.let { r.rewrite(it) }
        dsl.caseInsensitive?.let { r.caseInsensitive(it) }
        dsl.boost?.let { r.boost(it) }
        dsl._name?.let { r.queryName(it) }
        r
    }
}

// --- Fuzzy Query DSL ---
class FuzzyQueryDsl {
    var field: String? = null
    var value: Any? = null
    var fuzziness: String? = null
    var prefixLength: Int? = null
    var maxExpansions: Int? = null
    var transpositions: Boolean? = null
    var rewrite: String? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.fuzzyQuery(fn: FuzzyQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = FuzzyQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val v = dsl.value?.let { toFieldValue(it) } ?: return this
    return this.fuzzy { fq ->
        fq.field(f)
        fq.value(v)
        dsl.fuzziness?.let { fq.fuzziness(it) }
        dsl.prefixLength?.let { fq.prefixLength(it) }
        dsl.maxExpansions?.let { fq.maxExpansions(it) }
        dsl.transpositions?.let { fq.transpositions(it) }
        dsl.rewrite?.let { fq.rewrite(it) }
        dsl.boost?.let { fq.boost(it) }
        dsl._name?.let { fq.queryName(it) }
        fq
    }
}

private fun toFieldValue(value: Any): FieldValue? {
    return when (value) {
        is String -> value.takeIf { it.isNotBlank() }?.let { FieldValue.of(it) }
        is Int -> FieldValue.of(value.toLong())
        is Long -> FieldValue.of(value)
        is Short -> FieldValue.of(value.toLong())
        is Byte -> FieldValue.of(value.toLong())
        is Float -> FieldValue.of(value.toDouble())
        is Double -> FieldValue.of(value)
        is Boolean -> FieldValue.of(value)
        else -> null
    }
}

// --- IDs Query DSL ---
class IdsQueryDsl {
    var values: List<String?>? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.idsQuery(fn: IdsQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = IdsQueryDsl().apply(fn)
    val ids = dsl.values?.mapNotNull { it?.takeIf { v -> v.isNotBlank() } }?.takeIf { it.isNotEmpty() }
        ?: return this
    return this.ids { i ->
        i.values(ids)
        dsl.boost?.let { i.boost(it) }
        dsl._name?.let { i.queryName(it) }
        i
    }
}

// --- Terms Set Query DSL ---
class TermsSetQueryDsl {
    var field: String? = null
    var terms: List<String?>? = null
    var minimumShouldMatchField: String? = null
    var minimumShouldMatchScript: Script? = null
    var boost: Float? = null
    var _name: String? = null

    fun minimumShouldMatchScript(fn: Script.Builder.() -> Unit) {
        minimumShouldMatchScript = Script.Builder().apply(fn).build()
    }
}

fun Query.Builder.termsSetQuery(fn: TermsSetQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = TermsSetQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val os = dsl.terms?.mapNotNull { it?.takeIf { term -> term.isNotBlank() } }?.takeIf { it.isNotEmpty() }
        ?: return this
    return this.termsSet { ts ->
        ts.field(f)
        ts.terms(os)
        dsl.minimumShouldMatchField?.let { ts.minimumShouldMatchField(it) }
        dsl.minimumShouldMatchScript?.let { ts.minimumShouldMatchScript(it) }
        dsl.boost?.let { ts.boost(it) }
        dsl._name?.let { ts.queryName(it) }
        ts
    }
}
