package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.RankFeatureQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RankFeatureFunctionLinear
import co.elastic.clients.util.ObjectBuilder

class RankFeatureQueryDsl {
    var field: String? = null
    var boost: Float? = null
    var _name: String? = null

    private var functionConfig: RankFeatureFunctionConfig? = null

    fun saturation(fn: RankFeatureSaturationDsl.() -> Unit = {}) {
        functionConfig = RankFeatureFunctionConfig.Saturation(RankFeatureSaturationDsl().apply(fn))
    }

    fun logarithm(fn: RankFeatureLogarithmDsl.() -> Unit) {
        functionConfig = RankFeatureFunctionConfig.Logarithm(RankFeatureLogarithmDsl().apply(fn))
    }

    fun linear() {
        functionConfig = RankFeatureFunctionConfig.Linear
    }

    fun sigmoid(fn: RankFeatureSigmoidDsl.() -> Unit) {
        functionConfig = RankFeatureFunctionConfig.Sigmoid(RankFeatureSigmoidDsl().apply(fn))
    }

    internal fun buildFunctionApplier(): ((RankFeatureQuery.Builder) -> Unit)? {
        return when (val config = functionConfig) {
            null -> { _: RankFeatureQuery.Builder -> }
            RankFeatureFunctionConfig.Linear -> { builder ->
                builder.linear(RankFeatureFunctionLinear._INSTANCE)
            }
            is RankFeatureFunctionConfig.Saturation -> {
                { builder ->
                    builder.saturation { sat ->
                        config.dsl.pivot?.let { sat.pivot(it) }
                        sat
                    }
                }
            }
            is RankFeatureFunctionConfig.Logarithm -> {
                val scalingFactor = config.dsl.scalingFactor ?: return null
                { builder ->
                    builder.log { log ->
                        log.scalingFactor(scalingFactor)
                        log
                    }
                }
            }
            is RankFeatureFunctionConfig.Sigmoid -> {
                val pivot = config.dsl.pivot ?: return null
                val exponent = config.dsl.exponent ?: return null
                { builder ->
                    builder.sigmoid { sig ->
                        sig.pivot(pivot)
                        sig.exponent(exponent)
                        sig
                    }
                }
            }
        }
    }

    private sealed interface RankFeatureFunctionConfig {
        data class Saturation(val dsl: RankFeatureSaturationDsl) : RankFeatureFunctionConfig
        data class Logarithm(val dsl: RankFeatureLogarithmDsl) : RankFeatureFunctionConfig
        data class Sigmoid(val dsl: RankFeatureSigmoidDsl) : RankFeatureFunctionConfig
        data object Linear : RankFeatureFunctionConfig
    }
}

class RankFeatureSaturationDsl {
    var pivot: Float? = null
}

class RankFeatureLogarithmDsl {
    var scalingFactor: Float? = null
}

class RankFeatureSigmoidDsl {
    var pivot: Float? = null
    var exponent: Float? = null
}

fun Query.Builder.rankFeatureQuery(fn: RankFeatureQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = RankFeatureQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val functionApplier = dsl.buildFunctionApplier() ?: return this

    return this.rankFeature { rf ->
        rf.field(field)
        functionApplier(rf)
        dsl.boost?.let { rf.boost(it) }
        dsl._name?.let { rf.queryName(it) }
        rf
    }
}
