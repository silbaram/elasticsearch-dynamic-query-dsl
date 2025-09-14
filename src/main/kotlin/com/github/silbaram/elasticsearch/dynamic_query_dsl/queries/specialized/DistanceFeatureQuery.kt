package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized
import co.elastic.clients.elasticsearch._types.query_dsl.DistanceFeatureQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import co.elastic.clients.json.JsonData

/** DSL helper for Query.Builder usage */
class DistanceFeatureQueryDsl {
    var field: String? = null
    var origin: String? = null // for date origin or geo string
    var pivot: String? = null
    var boost: Float? = null
    var _name: String? = null

    internal var originLat: Double? = null
    internal var originLon: Double? = null

    fun origin(lat: Double, lon: Double) {
        this.originLat = lat
        this.originLon = lon
        this.origin = null
    }

}

/** Query.Builder extension for DSL usage */
fun Query.Builder.distanceFeatureQuery(fn: DistanceFeatureQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = DistanceFeatureQueryDsl().apply(fn)

    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val p = dsl.pivot?.takeIf { it.isNotBlank() } ?: return this
    val originJson: JsonData = when {
        dsl.originLat != null && dsl.originLon != null -> JsonData.of(mapOf("lat" to dsl.originLat!!, "lon" to dsl.originLon!!))
        dsl.origin?.isNotBlank() == true -> JsonData.of(dsl.origin!!)
        else -> return this
    }

    return this.distanceFeature { df ->
        df.field(f)
        df.pivot(JsonData.of(p))
        df.origin(originJson)
        dsl.boost?.let { df.boost(it) }
        dsl._name?.let { df.queryName(it) }
        df
    }
}
