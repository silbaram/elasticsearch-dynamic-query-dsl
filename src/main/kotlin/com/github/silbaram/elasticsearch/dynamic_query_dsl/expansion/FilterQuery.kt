package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import co.elastic.clients.util.ObjectBuilderBase

//fun BoolQuery.Builder.filterQuery(fn: Query.Builder.() -> ObjectBuilder<Query>): BoolQuery.Builder {
//    return this.filter(fn)
//}

fun BoolQuery.Builder.filterQuery(fn: List<Query.Builder>.() -> List<ObjectBuilder<Query>>): BoolQuery.Builder {
    fn.let {
        println(it.toString())
    }
    return this
}

fun BoolQuery.Builder.filterQuery(vararg values: Query): BoolQuery.Builder {
    return this.filter(values.toList())
}