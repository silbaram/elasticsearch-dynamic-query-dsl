package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.geo

import co.elastic.clients.elasticsearch._types.GeoBounds
import co.elastic.clients.elasticsearch._types.GeoDistanceType
import co.elastic.clients.elasticsearch._types.GeoLocation
import co.elastic.clients.elasticsearch._types.WktGeoBounds
import co.elastic.clients.elasticsearch._types.query_dsl.GeoExecution
import co.elastic.clients.elasticsearch._types.query_dsl.GeoValidationMethod
import co.elastic.clients.elasticsearch._types.query_dsl.FieldLookup
import co.elastic.clients.elasticsearch._types.query_dsl.GeoShapeFieldQuery
import co.elastic.clients.elasticsearch._types.GeoShapeRelation
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ShapeFieldQuery
import co.elastic.clients.json.JsonData
import co.elastic.clients.util.ObjectBuilder

class GeoPointDsl {
    var lat: Double? = null
    var lon: Double? = null
    var geohash: String? = null
    var text: String? = null
    var coordinates: List<Double>? = null

    fun coordinates(lon: Double, lat: Double) {
        this.coordinates = listOf(lon, lat)
    }

    internal fun build(): GeoLocation? {
        val latValue = lat
        val lonValue = lon
        if (latValue != null && lonValue != null) {
            return GeoLocation.of { geo ->
                geo.latlon { ll ->
                    ll.lat(latValue)
                    ll.lon(lonValue)
                }
            }
        }

        val geohashValue = geohash?.takeIf { it.isNotBlank() }
        if (geohashValue != null) {
            return GeoLocation.of { geo ->
                geo.geohash { gh -> gh.geohash(geohashValue) }
            }
        }

        val textValue = text?.takeIf { it.isNotBlank() }
        if (textValue != null) {
            return GeoLocation.of { geo -> geo.text(textValue) }
        }

        val coords = coordinates?.takeIf { it.size == 2 }
        if (coords != null) {
            val asDouble = coords.map { it.toDouble() }
            return GeoLocation.of { geo -> geo.coords(asDouble) }
        }

        return null
    }
}

class GeoDistanceQueryDsl {
    var field: String? = null
    var distance: String? = null
    var distanceType: GeoDistanceType? = null
    var validationMethod: GeoValidationMethod? = null
    var ignoreUnmapped: Boolean? = null
    var boost: Float? = null
    var _name: String? = null

    var location: GeoLocation? = null
    private var locationDsl: GeoPointDsl? = null

    fun location(fn: GeoPointDsl.() -> Unit) {
        locationDsl = GeoPointDsl().apply(fn)
        location = null
    }

    fun location(location: GeoLocation) {
        this.location = location
        locationDsl = null
    }

    internal fun buildLocation(): GeoLocation? = location ?: locationDsl?.build()
}

fun Query.Builder.geoDistanceQuery(fn: GeoDistanceQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = GeoDistanceQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val distance = dsl.distance?.takeIf { it.isNotBlank() } ?: return this
    val location = dsl.buildLocation() ?: return this

    return this.geoDistance { g ->
        g.field(field)
        g.distance(distance)
        g.location(location)
        dsl.distanceType?.let { g.distanceType(it) }
        dsl.validationMethod?.let { g.validationMethod(it) }
        dsl.ignoreUnmapped?.let { g.ignoreUnmapped(it) }
        dsl.boost?.let { g.boost(it) }
        dsl._name?.let { g.queryName(it) }
        g
    }
}

class GeoBoundingBoxQueryDsl {
    var field: String? = null
    var type: GeoExecution? = null
    var validationMethod: GeoValidationMethod? = null
    var ignoreUnmapped: Boolean? = null
    var boost: Float? = null
    var _name: String? = null

    var bounds: GeoBounds? = null
    private var topLeftDsl: GeoPointDsl? = null
    private var bottomRightDsl: GeoPointDsl? = null
    private var wkt: String? = null

    fun topLeft(fn: GeoPointDsl.() -> Unit) {
        topLeftDsl = GeoPointDsl().apply(fn)
        bounds = null
        wkt = null
    }

    fun bottomRight(fn: GeoPointDsl.() -> Unit) {
        bottomRightDsl = GeoPointDsl().apply(fn)
        bounds = null
        wkt = null
    }

    fun boundingBox(bounds: GeoBounds) {
        this.bounds = bounds
        topLeftDsl = null
        bottomRightDsl = null
        wkt = null
    }

    fun wkt(value: String) {
        wkt = value
        bounds = null
        topLeftDsl = null
        bottomRightDsl = null
    }

    internal fun buildBounds(): GeoBounds? {
        bounds?.let { return it }
        val wktValue = wkt?.takeIf { it.isNotBlank() }
        if (wktValue != null) {
            return GeoBounds.of { b ->
                b.wkt { w -> w.wkt(wktValue) }
            }
        }
        val topLeft = topLeftDsl?.build()
        val bottomRight = bottomRightDsl?.build()
        if (topLeft != null && bottomRight != null) {
            return GeoBounds.of { b ->
                b.tlbr { tlbr ->
                    tlbr.topLeft(topLeft)
                    tlbr.bottomRight(bottomRight)
                }
            }
        }
        return null
    }
}

fun Query.Builder.geoBoundingBoxQuery(fn: GeoBoundingBoxQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = GeoBoundingBoxQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val bounds = dsl.buildBounds() ?: return this

    return this.geoBoundingBox { gb ->
        gb.field(field)
        gb.boundingBox(bounds)
        dsl.type?.let { gb.type(it) }
        dsl.validationMethod?.let { gb.validationMethod(it) }
        dsl.ignoreUnmapped?.let { gb.ignoreUnmapped(it) }
        dsl.boost?.let { gb.boost(it) }
        dsl._name?.let { gb.queryName(it) }
        gb
    }
}

class IndexedShapeDsl {
    var id: String? = null
    var index: String? = null
    var path: String? = null
    var routing: String? = null

    internal fun build(): FieldLookup? {
        val idValue = id?.takeIf { it.isNotBlank() } ?: return null
        return FieldLookup.Builder()
            .id(idValue)
            .apply {
                index?.takeIf { it.isNotBlank() }?.let { index(it) }
                path?.takeIf { it.isNotBlank() }?.let { path(it) }
                routing?.takeIf { it.isNotBlank() }?.let { routing(it) }
            }
            .build()
    }
}

class GeoShapeQueryDsl {
    var field: String? = null
    var relation: GeoShapeRelation? = null
    var ignoreUnmapped: Boolean? = null
    var boost: Float? = null
    var _name: String? = null

    var shape: JsonData? = null
    private var shapeSource: Any? = null
    var indexedShape: FieldLookup? = null
    private var indexedShapeDsl: IndexedShapeDsl? = null

    fun shape(value: JsonData) {
        shape = value
        shapeSource = null
    }

    fun shape(value: Any) {
        shapeSource = value
        shape = null
    }

    fun indexedShape(fn: IndexedShapeDsl.() -> Unit) {
        indexedShapeDsl = IndexedShapeDsl().apply(fn)
        indexedShape = null
    }

    fun indexedShape(fieldLookup: FieldLookup) {
        indexedShape = fieldLookup
        indexedShapeDsl = null
    }

    internal fun buildShapeData(): JsonData? = shape ?: shapeSource?.let { JsonData.of(it) }

    internal fun buildIndexedShape(): FieldLookup? = indexedShape ?: indexedShapeDsl?.build()
}

fun Query.Builder.geoShapeQuery(fn: GeoShapeQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = GeoShapeQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val shapeData = dsl.buildShapeData()
    val indexedShape = dsl.buildIndexedShape()
    if (shapeData == null && indexedShape == null) return this

    return this.geoShape { gs ->
        gs.field(field)
        val shapeBuilder = GeoShapeFieldQuery.Builder()
        shapeData?.let { shapeBuilder.shape(it) }
        indexedShape?.let { shapeBuilder.indexedShape(it) }
        dsl.relation?.let { shapeBuilder.relation(it) }
        gs.shape(shapeBuilder.build())
        dsl.ignoreUnmapped?.let { gs.ignoreUnmapped(it) }
        dsl.boost?.let { gs.boost(it) }
        dsl._name?.let { gs.queryName(it) }
        gs
    }
}

class ShapeQueryDsl {
    var field: String? = null
    var relation: GeoShapeRelation? = null
    var ignoreUnmapped: Boolean? = null
    var boost: Float? = null
    var _name: String? = null

    var shape: JsonData? = null
    private var shapeSource: Any? = null
    var indexedShape: FieldLookup? = null
    private var indexedShapeDsl: IndexedShapeDsl? = null

    fun shape(value: JsonData) {
        shape = value
        shapeSource = null
    }

    fun shape(value: Any) {
        shapeSource = value
        shape = null
    }

    fun indexedShape(fn: IndexedShapeDsl.() -> Unit) {
        indexedShapeDsl = IndexedShapeDsl().apply(fn)
        indexedShape = null
    }

    fun indexedShape(fieldLookup: FieldLookup) {
        indexedShape = fieldLookup
        indexedShapeDsl = null
    }

    internal fun buildShapeData(): JsonData? = shape ?: shapeSource?.let { JsonData.of(it) }

    internal fun buildIndexedShape(): FieldLookup? = indexedShape ?: indexedShapeDsl?.build()
}

fun Query.Builder.shapeQuery(fn: ShapeQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = ShapeQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val shapeData = dsl.buildShapeData()
    val indexedShape = dsl.buildIndexedShape()
    if (shapeData == null && indexedShape == null) return this

    return this.shape { sq ->
        sq.field(field)
        val shapeBuilder = ShapeFieldQuery.Builder()
        shapeData?.let { shapeBuilder.shape(it) }
        indexedShape?.let { shapeBuilder.indexedShape(it) }
        dsl.relation?.let { shapeBuilder.relation(it) }
        sq.shape(shapeBuilder.build())
        dsl.ignoreUnmapped?.let { sq.ignoreUnmapped(it) }
        dsl.boost?.let { sq.boost(it) }
        dsl._name?.let { sq.queryName(it) }
        sq
    }
}
