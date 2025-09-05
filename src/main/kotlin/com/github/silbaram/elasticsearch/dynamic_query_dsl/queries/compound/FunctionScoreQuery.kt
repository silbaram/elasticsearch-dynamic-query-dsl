package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorScoreFunction
import co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction
import co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction
import co.elastic.clients.elasticsearch._types.query_dsl.DecayFunction
import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders

/**
 * Function Score 쿼리를 위한 DSL 클래스
 */
class FunctionScoreQueryDsl {
    private val queryBuilders = SubQueryBuilders()
    private val functions = mutableListOf<co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore>()

    var scoreMode: FunctionScoreMode? = null
    var boostMode: FunctionBoostMode? = null
    var maxBoost: Double? = null
    var minScore: Double? = null
    var boost: Float? = null
    var _name: String? = null

    /**
     * 기본 쿼리 설정
     */
    fun query(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        queryBuilders.addAll(sub)
    }

    /**
     * 점수 계산 함수 추가
     */
    fun function(fn: FunctionScoreBuilder.() -> Unit) {
        val builder = FunctionScoreBuilder()
        builder.fn()
        builder.build()?.let { functions.add(it) }
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

    internal fun buildFunctions(): List<co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore>? {
        return if (functions.isEmpty()) null else functions
    }
}

/**
 * Function Score 개별 함수 빌더
 */
class FunctionScoreBuilder {
    private var filter: Query? = null
    private var weight: Double? = null
    private var fieldValueFactor: (() -> co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorScoreFunction)? = null
    private var scriptScore: (() -> co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction)? = null
    private var randomScore: (() -> co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction)? = null
    private var gaussDecay: ((String) -> co.elastic.clients.elasticsearch._types.query_dsl.DecayFunction)? = null
    private var expDecay: ((String) -> co.elastic.clients.elasticsearch._types.query_dsl.DecayFunction)? = null
    private var linearDecay: ((String) -> co.elastic.clients.elasticsearch._types.query_dsl.DecayFunction)? = null
    private var decayField: String? = null
    
    fun filter(query: Query) {
        this.filter = query
    }
    
    fun weight(value: Double) {
        this.weight = value
    }
    
    fun fieldValueFactor(fn: co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorScoreFunction.Builder.() -> Unit) {
        fieldValueFactor = {
            co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorScoreFunction.of { builder ->
                builder.fn()
                builder
            }
        }
    }
    
    fun scriptScore(fn: co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction.Builder.() -> Unit) {
        scriptScore = {
            co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction.of { builder ->
                builder.fn()
                builder
            }
        }
    }
    
    fun randomScore(fn: co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction.Builder.() -> Unit) {
        randomScore = {
            co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction.of { builder ->
                builder.fn()
                builder
            }
        }
    }
    
    fun build(): co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore? {
        // 어떤 스코어링 함수도 설정되지 않았으면 null 반환
        if (weight == null && fieldValueFactor == null && scriptScore == null && randomScore == null &&
            gaussDecay == null && expDecay == null && linearDecay == null) {
            return null
        }
        
        return co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore.of { f ->
            filter?.let { f.filter(it) }
            weight?.let { f.weight(it) }
            fieldValueFactor?.invoke()?.let { f.fieldValueFactor(it) }
            scriptScore?.invoke()?.let { f.scriptScore(it) }
            randomScore?.invoke()?.let { f.randomScore(it) }
            
            // decay 함수들 처리
            when {
                gaussDecay != null && decayField != null -> f.gauss(gaussDecay!!(decayField!!))
                expDecay != null && decayField != null -> f.exp(expDecay!!(decayField!!))
                linearDecay != null && decayField != null -> f.linear(linearDecay!!(decayField!!))
            }
            
            f
        }
    }
}

/**
 * Function Score 쿼리 확장 함수
 */
fun Query.Builder.functionScoreQuery(fn: FunctionScoreQueryDsl.() -> Unit): Query.Builder {
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
    return this
}

// --- Field Value Factor DSL ---

/**
 * Field Value Factor 함수 (인라인 파라미터 방식)
 */
fun FunctionScoreBuilder.fieldValueFactorQuery(
    field: String? = null,
    modifier: String? = null,
    factor: Double? = null,
    missing: Double? = null
) {
    // 필드가 null이면 함수를 생성하지 않음 (동적 제외)
    if (field == null || field.isBlank()) return
    
    this.fieldValueFactor {
        field(field)
        modifier?.let { mapFieldValueFactorModifier(it)?.let { mod -> modifier(mod) } }
        factor?.let { factor(it) }
        missing?.let { missing(it) }
    }
}

/**
 * Field Value Factor modifier 문자열 매핑
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

// --- Script Score DSL ---

/**
 * Script Score 함수 (인라인 스크립트)
 */
fun FunctionScoreBuilder.scriptScoreQuery(
    source: String,
    params: Map<String, JsonData>? = null
) {
    this.scriptScore {
        script(
            co.elastic.clients.elasticsearch._types.Script.of { script ->
                script.inline { inline ->
                    inline.source(source)
                    params?.let { inline.params(it) }
                }
            }
        )
    }
}

/**
 * Script Score 함수 (Stored script)
 */
fun FunctionScoreBuilder.scriptScoreStoredQuery(
    id: String,
    params: Map<String, JsonData>? = null
) {
    this.scriptScore {
        script(
            co.elastic.clients.elasticsearch._types.Script.of { script ->
                script.stored { stored ->
                    stored.id(id)
                    params?.let { stored.params(it) }
                }
            }
        )
    }
}

// --- Random Score DSL ---

/**
 * Random Score 함수 (인라인 파라미터)
 */
fun FunctionScoreBuilder.randomScoreQuery(
    seed: String? = null,
    field: String? = null
) {
    this.randomScore {
        seed?.let { seed(it) }
        field?.let { field(it) }
    }
}

// --- Weight Function DSL ---

/**
 * Weight 함수
 */
fun FunctionScoreBuilder.weightQuery(weight: Double) {
    this.weight(weight)
}

// --- Decay Functions DSL ---
// TODO: Decay 함수들은 추후 구현

// --- String-based mapping functions ---

private fun mapFunctionScoreMode(value: String): FunctionScoreMode? {
    return when (value.trim().lowercase()) {
        "multiply" -> FunctionScoreMode.Multiply
        "sum" -> FunctionScoreMode.Sum
        "avg" -> FunctionScoreMode.Avg
        "first" -> FunctionScoreMode.First
        "max" -> FunctionScoreMode.Max
        "min" -> FunctionScoreMode.Min
        else -> null
    }
}

private fun mapFunctionBoostMode(value: String): FunctionBoostMode? {
    return when (value.trim().lowercase()) {
        "multiply" -> FunctionBoostMode.Multiply
        "replace" -> FunctionBoostMode.Replace
        "sum" -> FunctionBoostMode.Sum
        "avg" -> FunctionBoostMode.Avg
        "max" -> FunctionBoostMode.Max
        "min" -> FunctionBoostMode.Min
        else -> null
    }
}

/**
 * 문자열 기반 ScoreMode 설정
 */
fun FunctionScoreQueryDsl.scoreMode(mode: String): FunctionScoreQueryDsl {
    this.scoreMode = mapFunctionScoreMode(mode)
    return this
}

/**
 * 문자열 기반 BoostMode 설정
 */
fun FunctionScoreQueryDsl.boostMode(mode: String): FunctionScoreQueryDsl {
    this.boostMode = mapFunctionBoostMode(mode)
    return this
}