package com.gethomsefe.arch26.quakes

import kotlinx.serialization.Serializable

@Serializable
data class QuakeFeatureCollection(
    val type: String,
    val features: List<QuakeFeature>
)

@Serializable
data class QuakeFeature(
    val type: String,
    val geometry: QuakeGeometry,
    val properties: QuakeProperties
)

@Serializable
data class QuakeGeometry(
    val type: String,
    val coordinates: List<Double>
)

@Serializable
data class QuakeProperties(
    val publicID: String,
    val time: String,
    val depth: Double,
    val magnitude: Double,
    val locality: String,
    val mmi: Int,
    val quality: String
)
