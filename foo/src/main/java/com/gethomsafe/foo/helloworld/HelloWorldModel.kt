package com.gethomsafe.foo.helloworld

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Factory

object HelloWorldModel {
    interface Actions {
        fun increment()
    }

    data class State(
        val message: String,
        val count: Int,
        val actions: Actions
    )

    @Factory
    class Presenter {
        @Composable
        operator fun invoke(): State {
            var count by remember { mutableIntStateOf(0) }
            return State(
                message = "Hello, Architecture!",
                count = count,
                actions = object : Actions {
                    override fun increment() {
                        count++
                    }
                }
            )
        }
    }
}
