package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object EditModel {
    interface Actions {
        fun increment()
        fun decrement()
    }

    data class State(
        val count: Int,
        val actions: Actions
    )

    class Presenter {

        @Composable
        operator fun invoke(initialCount: Int): State {
            var count by remember { mutableIntStateOf(initialCount) }
            return State(count, object : Actions {
                override fun increment() { count++ }
                override fun decrement() { count-- }
            })
        }
    }
}
