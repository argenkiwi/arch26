package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed interface Worker<out I, out O>
data object Idle : Worker<Nothing, Nothing>
data class Busy<I>(val input: I) : Worker<I, Nothing>
data class Done<O>(val result: O) : Worker<Nothing, O>

typealias Runner = Worker<Unit, Unit>
typealias Saver<I> = Worker<I, Unit>
typealias Loader<O> = Worker<Unit, O>

@Composable
fun <I, O> rememberWorker(
    initial: Worker<I, O> = Idle,
    block: suspend (I) -> O
): ReadWriteProperty<Any?, Worker<I, O>> {
    var worker by remember { mutableStateOf(initial) }
    LaunchedEffect(worker) {
        (worker as? Busy)?.also { (input) ->
            worker = block(input).let(::Done)
        }
    }

    return object : ReadWriteProperty<Any?, Worker<I, O>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = worker
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Worker<I, O>) {
            worker = value
        }
    }
}

@Composable
fun rememberRunner(
    initial: Runner = Idle,
    block: suspend () -> Unit
): ReadWriteProperty<Any?, Runner> = rememberWorker(initial) { block() }

@Composable
fun <I> rememberSaver(
    initial: Saver<I> = Idle,
    block: suspend (I) -> Unit
): ReadWriteProperty<Any?, Saver<I>> = rememberWorker(initial, block)

@Composable
fun <O> rememberLoader(
    initial: Loader<O> = Idle,
    block: suspend () -> O
): ReadWriteProperty<Any?, Loader<O>> = rememberWorker(initial) { block() }
