package com.gethomsefe.arch26.quakes

import arrow.core.right
import com.gethomsefe.arch26.network.ErrorResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Singleton

@Singleton
class QuakesRepositoryImpl(private val geoNetApi: GeoNetApi) : QuakesRepository {
    private val cache = mutableMapOf<Int, MutableStateFlow<List<Quake>>>()
    private val emptyFlow by lazy { MutableStateFlow(emptyList<Quake>()) }
    override fun stateFlow(mmi: Int): StateFlow<List<Quake>> = cache[mmi] ?: emptyFlow
    override suspend fun fetch(mmi: Int) = geoNetApi.getQuakes(mmi).mapLeft { response ->
        when (response) {
            is ErrorResponse.Network -> response.throwable.run { message ?: toString() }
            is ErrorResponse.Server -> response.body
            is ErrorResponse.Unexpected -> response.throwable.run { message ?: toString() }
        }
    }.map { response ->
        val quakes = response.body.features.map { it.toDomain() }
        cache.getOrPut(mmi) { MutableStateFlow(emptyList()) }.value = quakes
        quakes
    }

    override suspend fun getOrFetch(mmi: Int) = when (val stateFlow = cache[mmi]) {
        null -> fetch(mmi)
        else -> stateFlow.value.right()
    }
}
