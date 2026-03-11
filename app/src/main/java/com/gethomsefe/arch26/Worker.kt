package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed interface Worker<out I, out O> {
    data object Idle : Worker<Nothing, Nothing>
    data class Busy<I>(val input: I) : Worker<I, Nothing>
    data class Done<O>(val result: O) : Worker<Nothing, O>
}

@Composable
fun <I, O> rememberWorker(
    initial: Worker<I, O> = Worker.Idle,
    block: suspend (I) -> O
): ReadWriteProperty<Any?, Worker<I, O>> {
    var worker by remember { mutableStateOf(initial) }
    LaunchedEffect(worker) {
        (worker as? Worker.Busy)?.also { (input) ->
            worker = block(input).let(Worker<I, O>::Done)
        }
    }

    return object : ReadWriteProperty<Any?, Worker<I, O>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = worker
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Worker<I, O>) {
            worker = value
        }
    }
}
