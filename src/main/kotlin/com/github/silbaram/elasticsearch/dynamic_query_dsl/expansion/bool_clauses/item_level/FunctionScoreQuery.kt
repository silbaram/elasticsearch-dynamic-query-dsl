package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.FunctionScoreQueryDsl
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

// bool 쿼리의 각 절에서 function_score를 사용할 수 있도록 하는 확장 함수
fun SubQueryBuilders.functionScoreQuery(
    fn: FunctionScoreQueryDsl.() -> Unit,
): SubQueryBuilders {
    val dsl = FunctionScoreQueryDsl().apply(fn)
    if (!dsl.hasValid()) return this

    val functionScoreQuery = Query.of { q ->
        q.functionScore { fs ->
            dsl.apply(fs)
            fs
        }
    }

    this.addQuery(functionScoreQuery)
    return this
}

