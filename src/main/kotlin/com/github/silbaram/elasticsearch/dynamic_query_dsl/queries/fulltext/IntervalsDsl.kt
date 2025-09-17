package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

/** DSL for building Elasticsearch intervals queries in builder style. */
class IntervalsRuleDsl {
    internal val intervals = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.Intervals>()
    internal var isAllOf = false
    internal var isAnyOf = false
    internal var maxGaps: Int? = null
    internal var ordered: Boolean? = null

    fun allOf(maxGaps: Int? = null, ordered: Boolean? = null, fn: IntervalsCollectionDsl.() -> Unit) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        intervals.addAll(dsl.intervals)
        this.isAllOf = true
        this.maxGaps = maxGaps
        this.ordered = ordered
    }

    fun anyOf(fn: IntervalsCollectionDsl.() -> Unit) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        intervals.addAll(dsl.intervals)
        this.isAnyOf = true
    }

    fun match(query: String, maxGaps: Int? = null, ordered: Boolean? = null, analyzer: String? = null, useField: String? = null) {
        if (query.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.match { match ->
            match.query(query)
            maxGaps?.let { match.maxGaps(it) }
            ordered?.let { match.ordered(it) }
            analyzer?.let { match.analyzer(it) }
            useField?.let { match.useField(it) }
            match
        }
        intervals.add(intervalBuilder.build())
    }

    fun prefix(prefix: String, analyzer: String? = null, useField: String? = null) {
        if (prefix.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.prefix { prefixRule ->
            prefixRule.prefix(prefix)
            analyzer?.let { prefixRule.analyzer(it) }
            useField?.let { prefixRule.useField(it) }
            prefixRule
        }
        intervals.add(intervalBuilder.build())
    }

    fun wildcard(pattern: String, analyzer: String? = null, useField: String? = null) {
        if (pattern.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.wildcard { wildcard ->
            wildcard.pattern(pattern)
            analyzer?.let { wildcard.analyzer(it) }
            useField?.let { wildcard.useField(it) }
            wildcard
        }
        intervals.add(intervalBuilder.build())
    }

    fun fuzzy(term: String, prefixLength: Int? = null, transpositions: Boolean? = null, fuzziness: String? = null, analyzer: String? = null, useField: String? = null) {
        if (term.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.fuzzy { fuzzy ->
            fuzzy.term(term)
            prefixLength?.let { fuzzy.prefixLength(it) }
            transpositions?.let { fuzzy.transpositions(it) }
            fuzziness?.let { fuzzy.fuzziness(it) }
            analyzer?.let { fuzzy.analyzer(it) }
            useField?.let { fuzzy.useField(it) }
            fuzzy
        }
        intervals.add(intervalBuilder.build())
    }
}

class IntervalsCollectionDsl {
    internal val intervals = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.Intervals>()

    fun match(query: String, maxGaps: Int? = null, ordered: Boolean? = null, analyzer: String? = null, useField: String? = null) {
        if (query.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.match { match ->
            match.query(query)
            maxGaps?.let { match.maxGaps(it) }
            ordered?.let { match.ordered(it) }
            analyzer?.let { match.analyzer(it) }
            useField?.let { match.useField(it) }
            match
        }
        intervals.add(intervalBuilder.build())
    }

    fun prefix(prefix: String, analyzer: String? = null, useField: String? = null) {
        if (prefix.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.prefix { prefixRule ->
            prefixRule.prefix(prefix)
            analyzer?.let { prefixRule.analyzer(it) }
            useField?.let { prefixRule.useField(it) }
            prefixRule
        }
        intervals.add(intervalBuilder.build())
    }

    fun wildcard(pattern: String, analyzer: String? = null, useField: String? = null) {
        if (pattern.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.wildcard { wildcard ->
            wildcard.pattern(pattern)
            analyzer?.let { wildcard.analyzer(it) }
            useField?.let { wildcard.useField(it) }
            wildcard
        }
        intervals.add(intervalBuilder.build())
    }

    fun fuzzy(term: String, prefixLength: Int? = null, transpositions: Boolean? = null, fuzziness: String? = null, analyzer: String? = null, useField: String? = null) {
        if (term.isBlank()) return
        val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
        intervalBuilder.fuzzy { fuzzy ->
            fuzzy.term(term)
            prefixLength?.let { fuzzy.prefixLength(it) }
            transpositions?.let { fuzzy.transpositions(it) }
            fuzziness?.let { fuzzy.fuzziness(it) }
            analyzer?.let { fuzzy.analyzer(it) }
            useField?.let { fuzzy.useField(it) }
            fuzzy
        }
        intervals.add(intervalBuilder.build())
    }
}

fun Query.Builder.intervals(field: String, boost: Float? = null, _name: String? = null, fn: IntervalsRuleDsl.() -> Unit): ObjectBuilder<Query> {
    if (field.isBlank()) return this
    val dsl = IntervalsRuleDsl().apply(fn)
    if (dsl.intervals.isEmpty()) return this

    return this.intervals { iv ->
        iv.field(field)
        when {
            dsl.isAllOf -> iv.allOf { allOf ->
                dsl.intervals.forEach { allOf.intervals(it) }
                dsl.maxGaps?.let { allOf.maxGaps(it) }
                dsl.ordered?.let { allOf.ordered(it) }
                allOf
            }
            dsl.isAnyOf || dsl.intervals.size > 1 -> iv.anyOf { anyOf ->
                dsl.intervals.forEach { anyOf.intervals(it) }
                anyOf
            }
            dsl.intervals.size == 1 -> {
                // 단일 규칙은 any_of 래핑 없이 직접 매핑한다
                val single = dsl.intervals.first()
                var applied = false
                // 가능한 variant들을 직접 설정 (클라이언트의 variant getter를 사용)
                // match/prefix/wildcard/fuzzy 중 하나만 설정되어 있음
                single.match()?.let { m -> iv.match(m); applied = true }
                single.prefix()?.let { p -> iv.prefix(p); applied = true }
                single.wildcard()?.let { w -> iv.wildcard(w); applied = true }
                single.fuzzy()?.let { f -> iv.fuzzy(f); applied = true }
                if (!applied) {
                    // 혹시 위 매핑이 불가능한 클라이언트 버전이면 any_of로 폴백
                    iv.anyOf { any -> any.intervals(single); any }
                }
                iv
            }
        }
        boost?.let { iv.boost(it) }
        _name?.let { iv.queryName(it) }
        iv
    }
}
