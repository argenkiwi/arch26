package com.gethomsefe.arch26.quakes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gethomsefe.arch26.Busy
import com.gethomsefe.arch26.Worker
import com.gethomsefe.arch26.rememberWorker
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

object QuakesModel {
    sealed interface Action {
        data class SetMmi(val mmi: Int) : Action
        data object Refresh : Action
    }

    data class State(
        val quakes: Worker<Int, List<Quake>>,
        val mmi: Int,
        val perform: (Action) -> Unit
    )

    class Presenter(private val store: Store<Int, List<QuakeFeature>>) {
        @Composable
        operator fun invoke(): State {
            var mmi by remember { mutableIntStateOf(3) }
            var quakes by rememberWorker(initial = Busy(mmi)) { mmiValue ->
                store.fresh(mmiValue).map { it.toDomain() }
            }

            return State(
                quakes = quakes,
                mmi = mmi,
                perform = { action ->
                    when (action) {
                        is Action.SetMmi -> {
                            mmi = action.mmi
                            quakes = Busy(action.mmi)
                        }

                        Action.Refresh -> quakes = Busy(mmi)
                    }
                }
            )
        }
    }
}
