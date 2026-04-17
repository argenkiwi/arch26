package com.gethomsefe.arch26.quakes.data

data class Quake(
    val id: String,
    val time: String,
    val depth: Double,
    val magnitude: Double,
    val locality: String,
    val mmi: Int,
    val latitude: Double,
    val longitude: Double,
    val quality: String
)