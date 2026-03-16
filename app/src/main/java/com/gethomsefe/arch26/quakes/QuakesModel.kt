package com.gethomsefe.arch26.quakes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import com.gethomsefe.arch26.Busy
import com.gethomsefe.arch26.Worker
import com.gethomsefe.arch26.rememberWorker
import com.gethomsefe.arch26.network.ErrorResponse

object QuakesModel {
    sealed interface Action {
        data class SetMmi(val mmi: Int) : Action
        data object Refresh : Action
    }

    data class State(
        val quakesWorker: Worker<Int, Either<String?, List<Quake>>>,
        val mmi: Int,
        val perform: (Action) -> Unit
    )

    class Presenter(private val geoNetApi: GeoNetApi) {

        @Composable
        operator fun invoke(): State {
            var mmi by remember { mutableIntStateOf(3) }
            var quakesWorker by rememberWorker(initial = Busy(mmi)) { mmiValue ->
                geoNetApi.getQuakes(mmiValue).mapLeft { response ->
                    when (response) {
                        is ErrorResponse.Network -> response.throwable.message
                        is ErrorResponse.Server -> response.body
                        is ErrorResponse.Unexpected -> response.throwable.message
                    }
                }.map { response ->
                    response.body.features.map { it.toDomain() }
                }
            }

            return State(
                quakesWorker = quakesWorker,
                mmi = mmi,
                perform = { action ->
                    when (action) {
                        is Action.SetMmi -> {
                            mmi = action.mmi
                            quakesWorker = Busy(action.mmi)
                        }

                        Action.Refresh -> quakesWorker = Busy(mmi)
                    }
                }
            )
        }
    }
}
