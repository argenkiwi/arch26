package com.gethomsefe.arch26.quakes.data

fun QuakeFeature.toDomain(): Quake = Quake(
    id = properties.publicID,
    time = properties.time,
    depth = properties.depth,
    magnitude = properties.magnitude,
    locality = properties.locality,
    mmi = properties.mmi,
    latitude = geometry.coordinates.getOrElse(1) { 0.0 },
    longitude = geometry.coordinates.getOrElse(0) { 0.0 },
    quality = properties.quality
)
