package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.json.JsonData
import co.elastic.clients.util.ObjectBuilder

/** 공통 스크립트 빌드 옵션을 제공하는 베이스 DSL */
open class ScriptOptionsDsl {
    var source: String? = null
    var lang: String? = null
    var id: String? = null
    var providedScript: Script? = null

    private val paramsBacking = linkedMapOf<String, Any?>()

    var params: Map<String, Any?>?
        get() = paramsBacking.takeIf { it.isNotEmpty() }?.toMap()
        set(value) {
            paramsBacking.clear()
            value?.forEach { (key, rawValue) ->
                val trimmedKey = key.trim()
                if (trimmedKey.isEmpty() || rawValue == null) return@forEach
                paramsBacking[trimmedKey] = rawValue
            }
        }

    fun params(vararg entries: Pair<String, Any?>) {
        if (entries.isEmpty()) return
        entries.forEach { (key, rawValue) ->
            val trimmedKey = key.trim()
            if (trimmedKey.isEmpty()) return@forEach
            if (rawValue == null) {
                paramsBacking.remove(trimmedKey)
            } else {
                paramsBacking[trimmedKey] = rawValue
            }
        }
    }

    fun inline(source: String, lang: String? = null, params: Map<String, Any?>? = null) {
        this.source = source
        this.lang = lang
        this.id = null
        this.params = params
    }

    fun stored(id: String, params: Map<String, Any?>? = null) {
        this.id = id
        this.source = null
        this.lang = null
        this.params = params
    }

    fun clearParams() {
        paramsBacking.clear()
    }

    fun script(fn: Script.Builder.() -> Unit) {
        providedScript = Script.of { builder ->
            builder.fn()
            builder
        }
    }

    internal fun buildScript(): Script? {
        providedScript?.let { return it }

        val storedId = id?.takeIf { it.isNotBlank() }
        val inlineSource = source?.takeIf { it.isNotBlank() }
        val params = buildParams()

        return when {
            storedId != null -> Script.of { script ->
                script.id(storedId)
                params?.let { script.params(it) }
            }
            inlineSource != null -> Script.of { script ->
                script.source(inlineSource)
                lang?.takeIf { it.isNotBlank() }?.let { script.lang(it) }
                params?.let { script.params(it) }
            }
            else -> null
        }
    }

    private fun buildParams(): Map<String, JsonData>? {
        if (paramsBacking.isEmpty()) return null

        val converted = linkedMapOf<String, JsonData>()
        paramsBacking.forEach { (key, rawValue) ->
            val trimmedKey = key.trim()
            if (trimmedKey.isEmpty() || rawValue == null) return@forEach
            val jsonValue = when (rawValue) {
                is JsonData -> rawValue
                else -> runCatching { JsonData.of(rawValue) }.getOrNull()
            }
            jsonValue?.let { converted[trimmedKey] = it }
        }
        return converted.takeIf { it.isNotEmpty() }
    }
}

/** DSL helper for building script queries */
class ScriptQueryDsl : ScriptOptionsDsl() {
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.scriptQuery(fn: ScriptQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = ScriptQueryDsl().apply(fn)
    val script = dsl.buildScript() ?: return this

    return this.script { sq ->
        sq.script(script)
        dsl.boost?.let { sq.boost(it) }
        dsl._name?.let { sq.queryName(it) }
        sq
    }
}
