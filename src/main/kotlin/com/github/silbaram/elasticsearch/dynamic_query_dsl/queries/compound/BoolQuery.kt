package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

fun Query.Builder.boolQuery(
    fn: BoolQuery.Builder.() -> ObjectBuilder<BoolQuery>
): ObjectBuilder<Query> {
    return this.bool(fn)
}
