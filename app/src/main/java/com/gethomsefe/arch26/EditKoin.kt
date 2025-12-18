package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

object EditKoin {
    val module
        get() = module { factory { EditModel.Presenter() } }

    @Composable
    context(koin: KoinComponent)
    fun state(
        count: Int = 0,
        scope: CoroutineScope = retain { CoroutineScope(AndroidUiDispatcher.Main) }
    ): EditModel.State {
        val present = retain { koin.get<EditModel.Presenter>() }
        val stateFlow = retain(count) {
            scope.launchMolecule(
                mode = RecompositionMode.ContextClock,
                context = AndroidUiDispatcher.Main
            ) {
                present(count)
            }
        }

        val state by stateFlow.collectAsStateWithLifecycle()
        return state
    }
}
