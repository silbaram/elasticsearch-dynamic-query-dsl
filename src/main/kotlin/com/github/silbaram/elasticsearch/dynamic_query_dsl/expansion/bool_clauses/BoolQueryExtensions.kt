package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

/**
 * Reflection based helper that invokes the appropriate BoolQuery.Builder method
 * (e.g. `must`, `filter`, `mustNot`, `should`) regardless of whether the Java
 * client expects a [List] or a vararg of [Query] instances.  This makes the DSL
 * resilient to signature changes across different versions of the Elasticsearch
 * Java client.
 */
internal fun BoolQuery.Builder.addClause(methodName: String, queries: List<Query>): BoolQuery.Builder {
    if (queries.isEmpty()) return this
    val builderClass = this::class.java

    // Prefer a method accepting a java.util.List
    val listMethod = builderClass.methods.firstOrNull { method ->
        method.name == methodName &&
            method.parameterTypes.size == 1 &&
            java.util.List::class.java.isAssignableFrom(method.parameterTypes[0])
    }
    if (listMethod != null) {
        listMethod.invoke(this, queries)
        return this
    }

    // Fallback to a method accepting an array of Query
    val arrayMethod = builderClass.methods.firstOrNull { method ->
        method.name == methodName &&
            method.parameterTypes.size == 1 &&
            method.parameterTypes[0].isArray &&
            method.parameterTypes[0].componentType == Query::class.java
    }
    if (arrayMethod != null) {
        arrayMethod.invoke(this, queries.toTypedArray())
        return this
    }

    throw IllegalStateException("BoolQuery.Builder#$methodName accepting List or Array not found")
}
