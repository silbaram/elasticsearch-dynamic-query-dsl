package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorScoreFunction
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

class FunctionScoreQueryDsl {
    private val queryBuilders = SubQueryBuilders()
    private val functions = mutableListOf<FunctionScore.Builder>()

    var scoreMode: FunctionScoreMode? = null
    var boostMode: FunctionBoostMode? = null
    var maxBoost: Double? = null
    var minScore: Double? = null
    var boost: Float? = null
    var _name: String? = null

    fun query(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        queryBuilders.addAll(sub)
    }

    fun function(fn: FunctionScore.Builder.() -> Unit) {
        functions.add(FunctionScore.Builder().apply(fn))
    }

    internal fun buildQuery(): Query? {
        return when (queryBuilders.size()) {
            0 -> null
            1 -> {
                var result: Query? = null
                queryBuilders.forEach { result = it }
                result
            }
            else -> Query.of { q ->
                q.bool { b ->
                    queryBuilders.forEach { b.must(it) }
                    b
                }
            }
        }
    }

    internal fun buildFunctions(): List<FunctionScore>? {
        return if (functions.isEmpty()) null else functions.map { it.build() }
    }
}

fun Query.Builder.functionScoreQuery(fn: FunctionScoreQueryDsl.() -> Unit) {
    val dsl = FunctionScoreQueryDsl().apply(fn)
    this.functionScore { fs ->
        dsl.buildQuery()?.let { fs.query(it) }
        dsl.buildFunctions()?.let { fs.functions(it) }
        dsl.scoreMode?.let { fs.scoreMode(it) }
        dsl.boostMode?.let { fs.boostMode(it) }
        dsl.maxBoost?.let { fs.maxBoost(it) }
        dsl.minScore?.let { fs.minScore(it) }
        dsl.boost?.let { fs.boost(it) }
        dsl._name?.let { fs.queryName(it) }
        fs
    }
}

// --- Convenience DSL helpers to allow snake_case style and string modifiers ---

/**
 * Allows `function { field_value_factor { ... } }` JSON-like DSL.
 */
fun FunctionScore.Builder.fieldValueFactorQuery(fn: FieldValueFactorScoreFunction.Builder.() -> Unit): FunctionScore.Builder {
    this.fieldValueFactor { f ->
        f.apply(fn)
    }
    return this
}

/**
 * Overload to set common parameters inline like JSON.
 * Example: field_value_factor(field = "rating", modifier = "ln2p", factor = 1.5)
 */
fun FunctionScore.Builder.fieldValueFactorQuery(
    field: String? = null,
    modifier: String? = null,
    factor: Double? = null,
    missing: Double? = null
): FunctionScore.Builder {
    this.fieldValueFactor { f ->
        field?.let { f.field(it) }
        modifier?.let { mapFieldValueFactorModifier(it)?.let(f::modifier) }
        factor?.let { f.factor(it) }
        missing?.let { f.missing(it) }
    }
    return this
}

/**
 * Allows `modifier("ln2p")` inside the FieldValueFactor builder.
 */
fun FieldValueFactorScoreFunction.Builder.modifier(value: String): FieldValueFactorScoreFunction.Builder {
    mapFieldValueFactorModifier(value)?.let { this.modifier(it) }
    return this
}

private fun mapFieldValueFactorModifier(value: String): FieldValueFactorModifier? {
    return when (value.trim().lowercase()) {
        "none" -> FieldValueFactorModifier.None
        "log" -> FieldValueFactorModifier.Log
        "log1p" -> FieldValueFactorModifier.Log1p
        "log2p" -> FieldValueFactorModifier.Log2p
        "ln" -> FieldValueFactorModifier.Ln
        "ln1p" -> FieldValueFactorModifier.Ln1p
        "ln2p" -> FieldValueFactorModifier.Ln2p
        "sqrt" -> FieldValueFactorModifier.Sqrt
        "square" -> FieldValueFactorModifier.Square
        "reciprocal" -> FieldValueFactorModifier.Reciprocal
        else -> null
    }
}
