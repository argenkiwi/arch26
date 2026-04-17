package com.gethomsefe.arch26.quakes

import arrow.core.Either
import kotlinx.coroutines.flow.StateFlow

interface QuakesRepository {
    fun stateFlow(mmi: Int): StateFlow<List<Quake>>
    suspend fun getOrFetch(mmi: Int): Either<String, List<Quake>>
    suspend fun fetch(mmi: Int): Either<String, List<Quake>>
}
