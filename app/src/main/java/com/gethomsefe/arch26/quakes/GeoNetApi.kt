package com.gethomsefe.arch26.quakes

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders

class GeoNetApi(private val client: HttpClient) {
    suspend fun getQuakes(mmi: Int): QuakeFeatureCollection {
        return client.get("https://api.geonet.org.nz/quake") {
            parameter("MMI", mmi)
            header(HttpHeaders.Accept, "application/vnd.geo+json;version=2")
        }.body()
    }
}
