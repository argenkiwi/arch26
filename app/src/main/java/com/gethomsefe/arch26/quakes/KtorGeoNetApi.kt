package com.gethomsefe.arch26.quakes

import com.gethomsefe.arch26.network.ktorRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders

class KtorGeoNetApi(private val client: HttpClient) : GeoNetApi {
    override suspend fun getQuakes(mmi: Int) = ktorRequest<QuakeFeatureCollection, String> {
        client.get("https://api.geonet.org.nz/quake") {
            parameter("MMI", mmi)
            header(HttpHeaders.Accept, "application/vnd.geo+json;version=2")
        }
    }

}

