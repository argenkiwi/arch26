package com.gethomsefe.arch26.quakes

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val quakesModule
    get() = module {
        single {
            HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }

        single<GeoNetApi> { KtorGeoNetApi(get()) }
        factory { QuakesModel.Presenter(get()) }
    }
