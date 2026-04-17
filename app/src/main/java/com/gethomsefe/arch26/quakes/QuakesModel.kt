package com.gethomsefe.arch26.quakes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.gethomsefe.arch26.Busy
import com.gethomsefe.arch26.Worker
import com.gethomsefe.arch26.quakes.data.Quake
import com.gethomsefe.arch26.quakes.data.QuakesRepository
import com.gethomsefe.arch26.rememberWorker
import org.koin.core.annotation.Factory

object QuakesModel {
    sealed interface Action {
        data class SetMmi(val mmi: Int) : Action
        data object Refresh : Action
    }

    data class State(
        val mmi: Int,
        val worker: Worker<Boolean, Either<String?, List<Quake>>>,
        val quakes: List<Quake>,
        val perform: (Action) -> Unit
    )

    @Factory
    class Presenter(private val repository: QuakesRepository) {

        @Composable
        operator fun invoke(): State {
            var mmi by remember { mutableIntStateOf(3) }
            var loader by rememberWorker(Busy(false)) { refresh: Boolean ->
                with(repository) {
                    when {
                        refresh -> fetch(mmi)
                        else -> getOrFetch(mmi)
                    }
                }
            }

            LaunchedEffect(mmi) { loader = Busy(false) }
            val quakes by repository.stateFlow(mmi).collectAsState()
            val perform = { action: Action ->
                when (action) {
                    is Action.SetMmi -> mmi = action.mmi
                    Action.Refresh -> loader = Busy(true)
                }
            }

            return State(
                mmi = mmi,
                worker = loader,
                quakes = quakes,
                perform = perform
            )
        }
    }
}
