package com.gethomsefe.arch26.quakes

import com.gethomsefe.arch26.network.NetworkResponse

interface GeoNetApi {
    suspend fun getQuakes(mmi: Int): NetworkResponse<out QuakeFeatureCollection, String>
}
