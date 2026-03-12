package com.gethomsefe.arch26.quakes

import kotlinx.coroutines.flow.MutableStateFlow
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import java.util.concurrent.ConcurrentHashMap

private val cache = ConcurrentHashMap<Int, MutableStateFlow<List<QuakeFeature>?>>()

fun createQuakesStore(api: GeoNetApi): Store<Int, List<QuakeFeature>> = StoreBuilder.from(
    fetcher = Fetcher.of { mmi: Int -> api.getQuakes(mmi) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { mmi: Int -> cache.getOrPut(mmi) { MutableStateFlow(null) } },
        writer = { mmi: Int, quakeFeatureCollection: QuakeFeatureCollection ->
            cache.getOrPut(mmi) { MutableStateFlow(null) }.value =
                quakeFeatureCollection.features
        },
        delete = { mmi: Int -> cache.remove(mmi) },
        deleteAll = { cache.clear() }
    )
).build()
