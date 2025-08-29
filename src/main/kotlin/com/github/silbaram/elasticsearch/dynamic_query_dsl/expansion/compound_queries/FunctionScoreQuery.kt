package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

/**
 * DSL for building a [FunctionScoreQuery].
 *
 * It follows the style of other query DSLs in the project. It allows configuring
 * an optional base [query] and multiple scoring [functions].
 */
class FunctionScoreQueryDsl {
    private val baseQueryBuilders = SubQueryBuilders()
    private val functions = mutableListOf<FunctionScore>()

    var boostMode: FunctionBoostMode? = null
    var scoreMode: FunctionScoreMode? = null
    var maxBoost: Double? = null
    var minScore: Double? = null
    var boost: Float? = null
    var _name: String? = null

    /**
     * Defines the base query for the function_score query.
     */
    fun query(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        baseQueryBuilders.addAll(sub)
    }

    /**
     * Adds function definitions to the query.
     */
    fun functions(fn: FunctionsDsl.() -> Unit) {
        val dsl = FunctionsDsl().apply(fn)
        this.functions.addAll(dsl.build())
    }

    internal fun buildQuery(): Query? {
        return when (baseQueryBuilders.size()) {
            0 -> null
            1 -> {
                var result: Query? = null
                baseQueryBuilders.forEach { result = it }
                result
            }
            else -> Query.of { q ->
                q.bool { b ->
                    baseQueryBuilders.forEach { b.must(it) }
                    b
                }
            }
        }
    }

    internal fun buildFunctions(): List<FunctionScore> = functions
}

class FunctionsDsl {
    private val functions = mutableListOf<FunctionScore>()

    fun function(fn: FunctionScoreFunctionDsl.() -> Unit) {
        val dsl = FunctionScoreFunctionDsl().apply(fn)
        val built = dsl.build()
        if (built != null) {
            functions.add(built)
        }
    }

    internal fun build(): List<FunctionScore> = functions
}

class FunctionScoreFunctionDsl {
    private val filterQueries = SubQueryBuilders()
    private val builder = FunctionScore.Builder()
    private var functionSet = false

    var weight: Double? = null

    fun filter(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        filterQueries.addAll(sub)
    }

    fun exp(fn: DecayFunction.Builder.() -> Unit) {
        builder.exp { d -> d.apply(fn); d }
        functionSet = true
    }

    fun gauss(fn: DecayFunction.Builder.() -> Unit) {
        builder.gauss { d -> d.apply(fn); d }
        functionSet = true
    }

    fun linear(fn: DecayFunction.Builder.() -> Unit) {
        builder.linear { d -> d.apply(fn); d }
        functionSet = true
    }

    fun fieldValueFactor(fn: FieldValueFactorScoreFunction.Builder.() -> Unit) {
        builder.fieldValueFactor { f -> f.apply(fn); f }
        functionSet = true
    }

    fun randomScore(fn: RandomScoreFunction.Builder.() -> Unit) {
        builder.randomScore { r -> r.apply(fn); r }
        functionSet = true
    }

    fun scriptScore(fn: ScriptScoreFunction.Builder.() -> Unit) {
        builder.scriptScore { s -> s.apply(fn); s }
        functionSet = true
    }

    internal fun build(): FunctionScore? {
        if (!functionSet) {
            return null
        }

        val filterQuery = when (filterQueries.size()) {
            0 -> null
            1 -> {
                var result: Query? = null
                filterQueries.forEach { result = it }
                result
            }
            else -> Query.of { q ->
                q.bool { b ->
                    filterQueries.forEach { b.must(it) }
                    b
                }
            }
        }
        filterQuery?.let { builder.filter(it) }
        weight?.let { builder.weight(it) }
        return builder.build()
    }
}

fun Query.Builder.functionScoreQuery(fn: FunctionScoreQueryDsl.() -> Unit) {
    val dsl = FunctionScoreQueryDsl().apply(fn)
    val query = dsl.buildQuery()
    val functions = dsl.buildFunctions()
    if (query == null && functions.isEmpty()) {
        return
    }
    this.functionScore { fs ->
        query?.let { fs.query(it) }
        if (functions.isNotEmpty()) {
            fs.functions(functions)
        }
        dsl.boostMode?.let { fs.boostMode(it) }
        dsl.scoreMode?.let { fs.scoreMode(it) }
        dsl.maxBoost?.let { fs.maxBoost(it) }
        dsl.minScore?.let { fs.minScore(it) }
        dsl.boost?.let { fs.boost(it) }
        dsl._name?.let { fs.queryName(it) }
        fs
    }
}

