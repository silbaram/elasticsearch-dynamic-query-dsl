package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TokenPruningConfig
import co.elastic.clients.elasticsearch._types.query_dsl.WeightedTokensQuery
import co.elastic.clients.util.ObjectBuilder

class WeightedTokensQueryDsl {
    var field: String? = null
    var boost: Float? = null
    var _name: String? = null

    private val tokens = linkedMapOf<String, Float>()
    private var pruning: TokenPruningConfigDsl? = null

    fun token(value: String?, weight: Number?) {
        val key = value?.trim()
        if (key.isNullOrEmpty() || weight == null) return
        tokens[key] = weight.toFloat()
    }

    fun tokens(vararg entries: Pair<String?, Number?>) {
        entries.forEach { (key, value) -> token(key, value) }
    }

    fun tokens(map: Map<String?, Number?>?) {
        map?.forEach { (key, value) -> token(key, value) }
    }

    fun pruningConfig(fn: TokenPruningConfigDsl.() -> Unit) {
        pruning = TokenPruningConfigDsl().apply(fn)
    }

    internal fun resolvedTokens(): Map<String, Float> = tokens.toMap()

    internal fun resolvedPruning(): TokenPruningConfig? = pruning?.build()
}

class TokenPruningConfigDsl {
    var tokensFreqRatioThreshold: Int? = null
    var tokensWeightThreshold: Float? = null
    var onlyScorePrunedTokens: Boolean? = null

    fun build(): TokenPruningConfig? {
        if (tokensFreqRatioThreshold == null && tokensWeightThreshold == null && onlyScorePrunedTokens == null) {
            return null
        }
        return TokenPruningConfig.Builder().apply {
            tokensFreqRatioThreshold?.let { tokensFreqRatioThreshold(it) }
            tokensWeightThreshold?.let { tokensWeightThreshold(it) }
            onlyScorePrunedTokens?.let { onlyScorePrunedTokens(it) }
        }.build()
    }
}

fun Query.Builder.weightedTokensQuery(fn: WeightedTokensQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = WeightedTokensQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val tokens = dsl.resolvedTokens().takeIf { it.isNotEmpty() } ?: return this

    return this.weightedTokens { builder: WeightedTokensQuery.Builder ->
        builder.field(field)
        builder.tokens(tokens)
        dsl.resolvedPruning()?.let { builder.pruningConfig(it) }
        dsl.boost?.let { builder.boost(it) }
        dsl._name?.let { builder.queryName(it) }
        builder
    }
}
