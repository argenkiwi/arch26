package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

object EditKoin {
    val module
        get() = module { factory { EditModel.Presenter() } }

    @Composable
    context(scope: CoroutineScope, koin: KoinComponent)
    fun state(count: Int = 0): EditModel.State {
        val state by retainMolecule(
            presenter = { koin.get<EditModel.Presenter>() },
            present = { it(count) },
            count
        ).collectAsStateWithLifecycle()
        return state
    }
}
