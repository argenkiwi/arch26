package com.gethomsefe.arch26.slots

import androidx.compose.runtime.Composable
import com.gethomsefe.arch26.Worker
import com.gethomsefe.arch26.rememberWorker
import kotlinx.coroutines.delay
import kotlin.random.Random

object SlotsModel {
    sealed interface Action {
        data object Pull : Action
    }

    data class State(
        val slot1: Worker<Unit, Char>,
        val slot2: Worker<Unit, Char>,
        val slot3: Worker<Unit, Char>,
        val perform: (Action) -> Unit
    )

    class Presenter {
        @Composable
        operator fun invoke(): State {
            var slot1 by rememberWorker<Unit, Char> {
                delay(1000)
                Random.nextInt(65, 91).toChar()
            }

            var slot2 by rememberWorker<Unit, Char> {
                delay(1500)
                Random.nextInt(65, 91).toChar()
            }

            var slot3 by rememberWorker<Unit, Char> {
                delay(2000)
                Random.nextInt(65, 91).toChar()
            }

            return State(
                slot1 = slot1,
                slot2 = slot2,
                slot3 = slot3,
                perform = {
                    when (it) {
                        Action.Pull -> {
                            slot1 = Worker.Busy(Unit)
                            slot2 = Worker.Busy(Unit)
                            slot3 = Worker.Busy(Unit)
                        }
                    }
                }
            )
        }
    }
}
