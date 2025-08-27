package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorScoreFunction
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction
import co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

class FunctionScoreQueryDsl {
    private var query: Query? = null
    private val functions = mutableListOf<FunctionScore>()

    var boostMode: FunctionBoostMode? = null
    var scoreMode: FunctionScoreMode? = null
    var maxBoost: Double? = null
    var minScore: Double? = null
    var boost: Float? = null
    var _name: String? = null

    fun query(fn: SubQueryBuilders.() -> Any?) {
        val subQuery = SubQueryBuilders()
        val result = subQuery.fn()
        if (result is Query) {
            subQuery.addQuery(result)
        }
        query = when (subQuery.size()) {
            0 -> null
            1 -> {
                var resultQuery: Query? = null
                subQuery.forEach { resultQuery = it }
                resultQuery
            }
            else -> Query.of { q ->
                q.bool { b ->
                    subQuery.forEach { b.must(it) }
                    b
                }
            }
        }
    }

    fun function(fn: ScoreFunctionDsl.() -> Unit) {
        val dsl = ScoreFunctionDsl().apply(fn)
        dsl.build()?.let { functions.add(it) }
    }

    internal fun buildQuery(): Query? = query

    internal fun buildFunctions(): List<FunctionScore> = functions
}

class ScoreFunctionDsl {
    private val filterQueries = SubQueryBuilders()
    var weight: Double? = null
    private var randomScore: RandomScoreFunction? = null
    private var fieldValueFactor: FieldValueFactorScoreFunction? = null
    private var scriptScore: ScriptScoreFunction? = null

    fun filter(fn: SubQueryBuilders.() -> Any?) {
        val subQuery = SubQueryBuilders()
        val result = subQuery.fn()
        if (result is Query) {
            subQuery.addQuery(result)
        }
        filterQueries.addAll(subQuery)
    }

    fun randomScore(fn: RandomScoreFunction.Builder.() -> Unit) {
        randomScore = RandomScoreFunction.Builder().apply(fn).build()
    }

    fun fieldValueFactor(fn: FieldValueFactorScoreFunction.Builder.() -> Unit) {
        fieldValueFactor = FieldValueFactorScoreFunction.Builder().apply(fn).build()
    }

    fun scriptScore(fn: ScriptScoreFunction.Builder.() -> Unit) {
        scriptScore = ScriptScoreFunction.Builder().apply(fn).build()
    }

    internal fun build(): FunctionScore? {
        if (weight == null && randomScore == null && fieldValueFactor == null && scriptScore == null) {
            return null
        }
        return FunctionScore.Builder().apply {
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
            filterQuery?.let { filter(it) }
            weight?.let { weight(it) }
            randomScore?.let { randomScore(it) }
            fieldValueFactor?.let { fieldValueFactor(it) }
            scriptScore?.let { scriptScore(it) }
        }.build()
    }
}

fun Query.Builder.functionScoreQuery(fn: FunctionScoreQueryDsl.() -> Unit) {
    val dsl = FunctionScoreQueryDsl().apply(fn)
    val innerQuery = dsl.buildQuery()
    this.functionScore { fs ->
        innerQuery?.let { fs.query(it) }
        val funcs = dsl.buildFunctions()
        if (funcs.isNotEmpty()) fs.functions(funcs)
        dsl.maxBoost?.let { fs.maxBoost(it) }
        dsl.minScore?.let { fs.minScore(it) }
        dsl.scoreMode?.let { fs.scoreMode(it) }
        dsl.boostMode?.let { fs.boostMode(it) }
        dsl.boost?.let { fs.boost(it) }
        dsl._name?.let { fs.queryName(it) }
        fs
    }
}

