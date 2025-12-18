package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object EditModel {
    sealed interface Action {
        data object Increment : Action
        data object Decrement : Action
    }

    data class State(
        val count: Int,
        val perform: (Action) -> Unit
    )

    class Presenter {

        @Composable
        operator fun invoke(initialCount: Int): State {
            var count by remember { mutableIntStateOf(initialCount) }
            return State(count) { action ->
                when (action) {
                    Action.Decrement -> count--
                    Action.Increment -> count++
                }
            }
        }
    }
}
