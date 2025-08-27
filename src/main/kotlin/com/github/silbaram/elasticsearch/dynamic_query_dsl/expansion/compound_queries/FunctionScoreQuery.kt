package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

// SubQueryBuilders의 쿼리들을 하나의 Query로 합칩니다.
private fun buildQuery(builder: SubQueryBuilders): Query? {
    return when (builder.size()) {
        0 -> null
        1 -> {
            var result: Query? = null
            builder.forEach { result = it }
            result
        }
        else -> Query.of { q ->
            q.bool { b ->
                builder.forEach { b.must(it) }
                b
            }
        }
    }
}

// function_score 의 functions 절을 구성하기 위한 DSL
class FunctionScoreFunctionsDsl {
    private val functions = mutableListOf<FunctionScore>()

    /**
     * weight 함수를 추가합니다.
     * @param weight 적용할 가중치 값
     * @param filter weight가 적용될 대상 문서를 선택하는 필터 쿼리
     */
    fun weight(weight: Double, filter: (SubQueryBuilders.() -> Any?)? = null) {
        val builder = FunctionScore.Builder().weight(weight)
        filter?.let {
            val sub = SubQueryBuilders()
            val result = sub.it()
            if (result is Query) sub.addQuery(result)
            buildQuery(sub)?.let { builder.filter(it) }
        }
        functions.add(builder.build())
    }

    /**
     * field_value_factor 함수를 추가합니다.
     * @param field 점수 계산에 사용할 필드
     * @param factor 곱해질 계수
     * @param missing 필드가 존재하지 않을 때 사용될 기본값
     * @param modifier 값에 적용할 변형 함수
     * @param filter 함수 적용 대상 문서를 선택하는 필터 쿼리
     * @param weight 함수에 적용할 가중치
     */
    fun fieldValueFactor(
        field: String,
        factor: Double? = null,
        missing: Double? = null,
        modifier: FieldValueFactorModifier? = null,
        filter: (SubQueryBuilders.() -> Any?)? = null,
        weight: Double? = null,
    ) {
        val builder = FunctionScore.Builder()
        builder.fieldValueFactor { fvf ->
            fvf.field(field)
            factor?.let { fvf.factor(it) }
            missing?.let { fvf.missing(it) }
            modifier?.let { fvf.modifier(it) }
            fvf
        }
        weight?.let { builder.weight(it) }
        filter?.let {
            val sub = SubQueryBuilders()
            val result = sub.it()
            if (result is Query) sub.addQuery(result)
            buildQuery(sub)?.let { builder.filter(it) }
        }
        functions.add(builder.build())
    }

    internal fun build(): List<FunctionScore> = functions
}

// function_score 쿼리 DSL
class FunctionScoreQueryDsl {
    private var baseQuery: Query? = null
    private val scoringFunctions = mutableListOf<FunctionScore>()

    var boost: Float? = null
    var boostMode: FunctionBoostMode? = null
    var scoreMode: FunctionScoreMode? = null
    var maxBoost: Double? = null
    var minScore: Double? = null
    var _name: String? = null

    /**
     * function_score의 query 절을 설정합니다.
     */
    fun query(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (result is Query) {
            sub.addQuery(result)
        }
        baseQuery = buildQuery(sub)
    }

    /**
     * function_score의 functions 절을 설정합니다.
     */
    fun functions(fn: FunctionScoreFunctionsDsl.() -> Unit) {
        val dsl = FunctionScoreFunctionsDsl().apply(fn)
        scoringFunctions.addAll(dsl.build())
    }

    internal fun hasValid(): Boolean = scoringFunctions.isNotEmpty()

    internal fun apply(builder: FunctionScoreQuery.Builder): FunctionScoreQuery.Builder {
        baseQuery?.let { builder.query(it) }
        if (scoringFunctions.isNotEmpty()) {
            builder.functions(scoringFunctions)
        }
        boost?.let { builder.boost(it) }
        boostMode?.let { builder.boostMode(it) }
        scoreMode?.let { builder.scoreMode(it) }
        maxBoost?.let { builder.maxBoost(it) }
        minScore?.let { builder.minScore(it) }
        _name?.let { builder.queryName(it) }
        return builder
    }
}

// 최상위 function_score 쿼리 확장 함수
fun Query.Builder.functionScoreQuery(fn: FunctionScoreQueryDsl.() -> Unit) {
    val dsl = FunctionScoreQueryDsl().apply(fn)
    if (!dsl.hasValid()) return
    this.functionScore { fs ->
        dsl.apply(fs)
        fs
    }
}

