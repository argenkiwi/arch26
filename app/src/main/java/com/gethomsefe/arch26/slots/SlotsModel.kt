package com.gethomsefe.arch26.slots

import androidx.compose.runtime.Composable
import arrow.core.Either
import com.gethomsefe.arch26.Loader
import com.gethomsefe.arch26.rememberLoader
import kotlinx.coroutines.delay
import kotlin.random.Random

object SlotsModel {
    sealed interface Action {
        data object Pull : Action
    }

    data class State(
        val slot1: Loader<Char>,
        val slot2: Loader<Char>,
        val slot3: Loader<Char>,
        val perform: (Action) -> Unit
    )

    class Presenter {
        @Composable
        operator fun invoke(): State {
            var slot1 by rememberLoader {
                Either.catch {
                    delay(1000)
                    Random.nextInt(65, 91).toChar()
                }
            }

            var slot2 by rememberLoader {
                Either.catch {
                    delay(2000)
                    Random.nextInt(65, 91).toChar()
                }
            }

            var slot3 by rememberLoader {
                Either.catch {
                    delay(3000)
                    Random.nextInt(65, 91).toChar()
                }
            }

            return State(
                slot1 = slot1,
                slot2 = slot2,
                slot3 = slot3,
                perform = {
                    when (it) {
                        Action.Pull -> {
                            slot1 = Loader.Busy
                            slot2 = Loader.Busy
                            slot3 = Loader.Busy
                        }
                    }
                }
            )
        }
    }
}
