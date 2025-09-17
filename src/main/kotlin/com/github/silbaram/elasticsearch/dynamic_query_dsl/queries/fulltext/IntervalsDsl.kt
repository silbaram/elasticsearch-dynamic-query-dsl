package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

/** DSL for building Elasticsearch intervals queries in builder style. */
class IntervalsRuleDsl {
    internal val intervals = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.Intervals>()
    internal val setters = mutableListOf<(co.elastic.clients.elasticsearch._types.query_dsl.IntervalsQuery.Builder) -> Unit>()
    internal var isAllOf = false
    internal var isAnyOf = false
    internal var maxGaps: Int? = null
    internal var ordered: Boolean? = null

    fun allOf(maxGaps: Int? = null, ordered: Boolean? = null, fn: IntervalsCollectionDsl.() -> Unit) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        intervals.addAll(dsl.intervals)
        setters.addAll(dsl.setters)
        this.isAllOf = true
        this.maxGaps = maxGaps
        this.ordered = ordered
    }

    fun anyOf(fn: IntervalsCollectionDsl.() -> Unit) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        intervals.addAll(dsl.intervals)
        setters.addAll(dsl.setters)
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
        setters.add { iv ->
            iv.match { mb ->
                mb.query(query)
                maxGaps?.let { mg -> mb.maxGaps(mg) }
                ordered?.let { ord -> mb.ordered(ord) }
                analyzer?.let { a -> mb.analyzer(a) }
                useField?.let { uf -> mb.useField(uf) }
                mb
            }
        }
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
        setters.add { iv ->
            iv.prefix { pb ->
                pb.prefix(prefix)
                analyzer?.let { a -> pb.analyzer(a) }
                useField?.let { uf -> pb.useField(uf) }
                pb
            }
        }
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
        setters.add { iv ->
            iv.wildcard { wb ->
                wb.pattern(pattern)
                analyzer?.let { a -> wb.analyzer(a) }
                useField?.let { uf -> wb.useField(uf) }
                wb
            }
        }
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
        setters.add { iv ->
            iv.fuzzy { fb ->
                fb.term(term)
                prefixLength?.let { pl -> fb.prefixLength(pl) }
                transpositions?.let { tr -> fb.transpositions(tr) }
                fuzziness?.let { fu -> fb.fuzziness(fu) }
                analyzer?.let { a -> fb.analyzer(a) }
                useField?.let { uf -> fb.useField(uf) }
                fb
            }
        }
    }
}

class IntervalsCollectionDsl {
    internal val intervals = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.Intervals>()
    internal val setters = mutableListOf<(co.elastic.clients.elasticsearch._types.query_dsl.IntervalsQuery.Builder) -> Unit>()

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
        setters.add { iv ->
            iv.match { mb ->
                mb.query(query)
                maxGaps?.let { mg -> mb.maxGaps(mg) }
                ordered?.let { ord -> mb.ordered(ord) }
                analyzer?.let { a -> mb.analyzer(a) }
                useField?.let { uf -> mb.useField(uf) }
                mb
            }
        }
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
        setters.add { iv ->
            iv.prefix { pb ->
                pb.prefix(prefix)
                analyzer?.let { a -> pb.analyzer(a) }
                useField?.let { uf -> pb.useField(uf) }
                pb
            }
        }
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
        setters.add { iv ->
            iv.wildcard { wb ->
                wb.pattern(pattern)
                analyzer?.let { a -> wb.analyzer(a) }
                useField?.let { uf -> wb.useField(uf) }
                wb
            }
        }
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
        setters.add { iv ->
            iv.fuzzy { fb ->
                fb.term(term)
                prefixLength?.let { pl -> fb.prefixLength(pl) }
                transpositions?.let { tr -> fb.transpositions(tr) }
                fuzziness?.let { fu -> fb.fuzziness(fu) }
                analyzer?.let { a -> fb.analyzer(a) }
                useField?.let { uf -> fb.useField(uf) }
                fb
            }
        }
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
                // 단일 규칙은 any_of 래핑 없이 직접 매핑한다 (사전에 저장한 setter 사용)
                val setter = dsl.setters.firstOrNull()
                if (setter != null) {
                    setter(iv)
                } else {
                    // 폴백: any_of로 그대로 직렬화
                    val single = dsl.intervals.first()
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
