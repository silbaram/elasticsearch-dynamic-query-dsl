package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Query

/**
 * Advanced intervals query with DSL support for complex rule combinations
 */
fun intervalsQuery(
    field: String,
    boost: Float? = null,
    _name: String? = null,
    fn: IntervalsRuleDsl.() -> Unit
): Query? {
    if (field.isBlank()) return null
    
    val dsl = IntervalsRuleDsl().apply(fn)
    val rule = dsl.buildRule() ?: return null
    
    return Query.of { q ->
        q.intervals { intervals ->
            intervals.field(field)
            when {
                dsl.isAllOf -> intervals.allOf { allOf ->
                    dsl.intervals.forEach { allOf.intervals(it) }
                    dsl.maxGaps?.let { allOf.maxGaps(it) }
                    dsl.ordered?.let { allOf.ordered(it) }
                    allOf
                }
                dsl.isAnyOf -> intervals.anyOf { anyOf ->
                    dsl.intervals.forEach { anyOf.intervals(it) }
                    anyOf
                }
                dsl.intervals.size == 1 -> intervals.anyOf { anyOf ->
                    anyOf.intervals(dsl.intervals.first())
                    anyOf
                }
                else -> intervals.anyOf { anyOf ->
                    dsl.intervals.forEach { anyOf.intervals(it) }
                    anyOf
                }
            }
            boost?.let { intervals.boost(it) }
            _name?.let { intervals.queryName(it) }
            intervals
        }
    }
}

/**
 * DSL class for building complex interval rules
 */
class IntervalsRuleDsl {
    internal val intervals = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.Intervals>()
    internal var isAllOf = false
    internal var isAnyOf = false
    internal var maxGaps: Int? = null
    internal var ordered: Boolean? = null
    
    /**
     * All intervals must match (allOf rule)
     */
    fun allOf(
        maxGaps: Int? = null,
        ordered: Boolean? = null,
        fn: IntervalsCollectionDsl.() -> Unit
    ) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        intervals.addAll(dsl.intervals)
        this.isAllOf = true
        this.maxGaps = maxGaps
        this.ordered = ordered
    }
    
    /**
     * Any interval can match (anyOf rule)
     */
    fun anyOf(fn: IntervalsCollectionDsl.() -> Unit) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        intervals.addAll(dsl.intervals)
        this.isAnyOf = true
    }
    
    /**
     * Single match rule
     */
    fun match(
        query: String,
        maxGaps: Int? = null,
        ordered: Boolean? = null,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (query.isNotBlank()) {
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
    }
    
    /**
     * Single prefix rule
     */
    fun prefix(
        prefix: String,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (prefix.isNotBlank()) {
            val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
            intervalBuilder.prefix { prefixRule ->
                prefixRule.prefix(prefix)
                analyzer?.let { prefixRule.analyzer(it) }
                useField?.let { prefixRule.useField(it) }
                prefixRule
            }
            intervals.add(intervalBuilder.build())
        }
    }
    
    /**
     * Single wildcard rule
     */
    fun wildcard(
        pattern: String,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (pattern.isNotBlank()) {
            val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
            intervalBuilder.wildcard { wildcard ->
                wildcard.pattern(pattern)
                analyzer?.let { wildcard.analyzer(it) }
                useField?.let { wildcard.useField(it) }
                wildcard
            }
            intervals.add(intervalBuilder.build())
        }
    }
    
    /**
     * Single fuzzy rule
     */
    fun fuzzy(
        term: String,
        prefixLength: Int? = null,
        transpositions: Boolean? = null,
        fuzziness: String? = null,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (term.isNotBlank()) {
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
    
    internal fun buildRule(): Any? {
        return if (intervals.isNotEmpty()) intervals else null
    }
}

/**
 * DSL class for building collections of intervals
 */
class IntervalsCollectionDsl {
    internal val intervals = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.Intervals>()
    
    /**
     * Add a match rule to the collection
     */
    fun match(
        query: String,
        maxGaps: Int? = null,
        ordered: Boolean? = null,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (query.isNotBlank()) {
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
    }
    
    /**
     * Add a prefix rule to the collection
     */
    fun prefix(
        prefix: String,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (prefix.isNotBlank()) {
            val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
            intervalBuilder.prefix { prefixRule ->
                prefixRule.prefix(prefix)
                analyzer?.let { prefixRule.analyzer(it) }
                useField?.let { prefixRule.useField(it) }
                prefixRule
            }
            intervals.add(intervalBuilder.build())
        }
    }
    
    /**
     * Add a wildcard rule to the collection
     */
    fun wildcard(
        pattern: String,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (pattern.isNotBlank()) {
            val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
            intervalBuilder.wildcard { wildcard ->
                wildcard.pattern(pattern)
                analyzer?.let { wildcard.analyzer(it) }
                useField?.let { wildcard.useField(it) }
                wildcard
            }
            intervals.add(intervalBuilder.build())
        }
    }
    
    /**
     * Add a fuzzy rule to the collection
     */
    fun fuzzy(
        term: String,
        prefixLength: Int? = null,
        transpositions: Boolean? = null,
        fuzziness: String? = null,
        analyzer: String? = null,
        useField: String? = null
    ) {
        if (term.isNotBlank()) {
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
    
    /**
     * Nested anyOf rule within this collection
     */
    fun anyOf(fn: IntervalsCollectionDsl.() -> Unit) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        if (dsl.intervals.isNotEmpty()) {
            val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
            intervalBuilder.anyOf { anyOf ->
                dsl.intervals.forEach { anyOf.intervals(it) }
                anyOf
            }
            intervals.add(intervalBuilder.build())
        }
    }
    
    /**
     * Nested allOf rule within this collection
     */
    fun allOf(
        maxGaps: Int? = null,
        ordered: Boolean? = null,
        fn: IntervalsCollectionDsl.() -> Unit
    ) {
        val dsl = IntervalsCollectionDsl().apply(fn)
        if (dsl.intervals.isNotEmpty()) {
            val intervalBuilder = co.elastic.clients.elasticsearch._types.query_dsl.Intervals.Builder()
            intervalBuilder.allOf { allOf ->
                dsl.intervals.forEach { allOf.intervals(it) }
                maxGaps?.let { allOf.maxGaps(it) }
                ordered?.let { allOf.ordered(it) }
                allOf
            }
            intervals.add(intervalBuilder.build())
        }
    }
}

/**
 * Simple intervals match query - 기본 match rule만 지원하는 간단한 버전
 */
fun intervalsMatchQuery(
    field: String,
    query: String?,
    maxGaps: Int? = null,
    ordered: Boolean? = null,
    analyzer: String? = null,
    useField: String? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (field.isBlank() || query.isNullOrEmpty()) {
        null
    } else {
        Query.of { q ->
            q.intervals { intervals ->
                intervals.field(field)
                intervals.anyOf { anyOf ->
                    anyOf.intervals { intervalsContainer ->
                        intervalsContainer.match { match ->
                            match.query(query)
                            maxGaps?.let { match.maxGaps(it) }
                            ordered?.let { match.ordered(it) }
                            analyzer?.let { match.analyzer(it) }
                            useField?.let { match.useField(it) }
                            match
                        }
                    }
                }
                boost?.let { intervals.boost(it) }
                _name?.let { intervals.queryName(it) }
                intervals
            }
        }
    }
}

/**
 * Simple intervals prefix query - 기본 prefix rule만 지원하는 간단한 버전
 */
fun intervalsPrefixQuery(
    field: String,
    prefix: String?,
    analyzer: String? = null,
    useField: String? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (field.isBlank() || prefix.isNullOrEmpty()) {
        null
    } else {
        Query.of { q ->
            q.intervals { intervals ->
                intervals.field(field)
                intervals.anyOf { anyOf ->
                    anyOf.intervals { intervalsContainer ->
                        intervalsContainer.prefix { prefixRule ->
                            prefixRule.prefix(prefix)
                            analyzer?.let { prefixRule.analyzer(it) }
                            useField?.let { prefixRule.useField(it) }
                            prefixRule
                        }
                    }
                }
                boost?.let { intervals.boost(it) }
                _name?.let { intervals.queryName(it) }
                intervals
            }
        }
    }
}

/**
 * Simple intervals wildcard query - 기본 wildcard rule만 지원하는 간단한 버전
 */
fun intervalsWildcardQuery(
    field: String,
    pattern: String?,
    analyzer: String? = null,
    useField: String? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (field.isBlank() || pattern.isNullOrEmpty()) {
        null
    } else {
        Query.of { q ->
            q.intervals { intervals ->
                intervals.field(field)
                intervals.anyOf { anyOf ->
                    anyOf.intervals { intervalsContainer ->
                        intervalsContainer.wildcard { wildcard ->
                            wildcard.pattern(pattern)
                            analyzer?.let { wildcard.analyzer(it) }
                            useField?.let { wildcard.useField(it) }
                            wildcard
                        }
                    }
                }
                boost?.let { intervals.boost(it) }
                _name?.let { intervals.queryName(it) }
                intervals
            }
        }
    }
}

/**
 * Simple intervals fuzzy query - 기본 fuzzy rule만 지원하는 간단한 버전
 */
fun intervalsFuzzyQuery(
    field: String,
    term: String?,
    prefixLength: Int? = null,
    transpositions: Boolean? = null,
    fuzziness: String? = null,
    analyzer: String? = null,
    useField: String? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (field.isBlank() || term.isNullOrEmpty()) {
        null
    } else {
        Query.of { q ->
            q.intervals { intervals ->
                intervals.field(field)
                intervals.anyOf { anyOf ->
                    anyOf.intervals { intervalsContainer ->
                        intervalsContainer.fuzzy { fuzzy ->
                            fuzzy.term(term)
                            prefixLength?.let { fuzzy.prefixLength(it) }
                            transpositions?.let { fuzzy.transpositions(it) }
                            fuzziness?.let { fuzzy.fuzziness(it) }
                            analyzer?.let { fuzzy.analyzer(it) }
                            useField?.let { fuzzy.useField(it) }
                            fuzzy
                        }
                    }
                }
                boost?.let { intervals.boost(it) }
                _name?.let { intervals.queryName(it) }
                intervals
            }
        }
    }
}