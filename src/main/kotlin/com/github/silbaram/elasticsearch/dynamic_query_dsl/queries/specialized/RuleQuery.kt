package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RuleQuery
import co.elastic.clients.json.JsonData
import co.elastic.clients.util.ObjectBuilder
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

class RuleQueryDsl {
    var boost: Float? = null
    var _name: String? = null
    var organic: Query? = null

    private var rulesetId: String? = null
    private var matchCriteria: JsonData? = null

    fun rulesetId(id: String?) {
        val trimmed = id?.trim()
        rulesetId = trimmed?.takeIf { it.isNotEmpty() }
    }

    fun rulesetIds(vararg ids: String?) {
        rulesetId(ids.firstOrNull { !it.isNullOrBlank() })
    }

    fun rulesetIds(ids: Iterable<String?>) {
        rulesetId(ids.firstOrNull { !it.isNullOrBlank() })
    }

    fun organic(fn: Query.Builder.() -> Unit) {
        organic = queryOrNull(fn)
    }

    fun matchCriteria(value: JsonData?) {
        matchCriteria = value
    }

    fun matchCriteria(value: Map<String, Any?>?) {
        if (value == null) {
            matchCriteria = null
            return
        }
        val sanitized = value.filterValues { it != null }
        if (sanitized.isEmpty()) {
            matchCriteria = null
            return
        }
        matchCriteria = JsonData.of(sanitized)
    }

    fun matchCriteria(value: Any?) {
        matchCriteria = value?.let { JsonData.of(it) }
    }

    fun clearMatchCriteria() {
        matchCriteria = null
    }

    internal fun resolvedRulesetId(): String? = rulesetId

    internal fun resolvedMatchCriteria(): JsonData? = matchCriteria
}

fun Query.Builder.ruleQueryDsl(fn: RuleQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = RuleQueryDsl().apply(fn)
    val rulesetId = dsl.resolvedRulesetId() ?: return this
    val organic = dsl.organic ?: return this
    val matchCriteria = dsl.resolvedMatchCriteria() ?: return this

    return this.ruleQuery { builder: RuleQuery.Builder ->
        builder.organic(organic)
        builder.rulesetId(rulesetId)
        builder.matchCriteria(matchCriteria)
        dsl.boost?.let { builder.boost(it) }
        dsl._name?.let { builder.queryName(it) }
        builder
    }
}
