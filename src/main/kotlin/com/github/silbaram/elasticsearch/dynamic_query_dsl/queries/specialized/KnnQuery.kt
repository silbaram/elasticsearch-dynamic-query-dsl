package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import co.elastic.clients.util.ObjectBuilder

class KnnQueryDsl {
    var field: String? = null
    var queryVector: List<Float?>? = null
    var k: Int? = null
    var numCandidates: Int? = null

    private val filterQueries = SubQueryBuilders()

    fun filter(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val res = sub.fn()
        if (sub.size() == 0 && res is Query) {
            sub.addQuery(res)
        }
        filterQueries.addAll(sub)
    }

    internal fun buildFilterQuery(): Query? {
        return when (filterQueries.size()) {
            0 -> null
            1 -> {
                var result: Query? = null
                filterQueries.forEach { result = it }
                result
            }
            else -> Query.of { q ->
                q.bool { b ->
                    // filter semantics for multiple filters
                    filterQueries.forEach { b.filter(it) }
                    b
                }
            }
        }
    }
}

fun Query.Builder.knnQuery(fn: KnnQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = KnnQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val vector = dsl.queryVector
        ?.takeIf { it.isNotEmpty() && it.all { value -> value != null } }
        ?.map { it!! }
        ?: return this
    val k = dsl.k?.let { provided ->
        provided.takeIf { it > 0 } ?: return this
    }
    val numCandidates = dsl.numCandidates?.takeIf { it > 0 } ?: return this

    return this.knn { kq ->
        kq.field(field)
        kq.queryVector(vector)
        fun trySetInt(name: String, value: Int) {
            listOfNotNull(Int::class.javaPrimitiveType, Int::class.javaObjectType).forEach { type ->
                try {
                    val method = kq.javaClass.getMethod(name, type)
                    method.invoke(kq, value)
                    return
                } catch (_: Exception) {
                    // try next signature (8.14에는 k 세터가 없어 무시)
                }
            }
        }
        k?.let { trySetInt("k", it) }
        kq.numCandidates(numCandidates)
        dsl.buildFilterQuery()?.let { fq -> kq.filter(fq) }
        kq
    }
}
