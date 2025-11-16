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

    private var rulesetIds: List<String> = emptyList()
    private var matchCriteria: JsonData? = null

    fun rulesetId(id: String?) {
        rulesetIds = sanitizeRulesetIds(listOf(id))
    }

    fun rulesetIds(vararg ids: String?) {
        rulesetIds = sanitizeRulesetIds(ids.asList())
    }

    fun rulesetIds(ids: Iterable<String?>) {
        rulesetIds = sanitizeRulesetIds(ids.asIterable())
    }

    fun clearRulesetIds() {
        rulesetIds = emptyList()
    }

    private fun sanitizeRulesetIds(candidates: Iterable<String?>): List<String> {
        return candidates
            .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
            .distinct()
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

    internal fun resolvedRulesetIds(): List<String> = rulesetIds

    internal fun resolvedMatchCriteria(): JsonData? = matchCriteria
}

fun Query.Builder.ruleQueryDsl(fn: RuleQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = RuleQueryDsl().apply(fn)
    val rulesetIds = dsl.resolvedRulesetIds()
    if (rulesetIds.isEmpty()) return this
    val organic = dsl.organic ?: return this
    val matchCriteria = dsl.resolvedMatchCriteria() ?: return this

    return this.rule { builder: RuleQuery.Builder ->
        builder.organic(organic)
        builder.rulesetIds(rulesetIds)
        builder.matchCriteria(matchCriteria)
        dsl.boost?.let { builder.boost(it) }
        dsl._name?.let { builder.queryName(it) }
        builder
    }
}
