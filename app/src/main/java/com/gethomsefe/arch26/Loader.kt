package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed interface Loader<out T> {
    data object Idle : Loader<Nothing>
    data object Busy : Loader<Nothing>
    data class Failure(val error: Throwable) : Loader<Nothing>
    data class Success<T>(val result: T) : Loader<T>
}

@Composable
fun <T> rememberLoader(
    initial: Loader<T> = Loader.Idle,
    block: suspend () -> Either<Throwable, T>
): ReadWriteProperty<Any?, Loader<T>> {
    var loader by remember { mutableStateOf(initial) }
    LaunchedEffect(loader) {
        (loader as? Loader.Busy)?.also { busy ->
            loader = block().fold({
                Loader.Failure(it)
            }) {
                Loader.Success(it)
            }
        }
    }

    return object : ReadWriteProperty<Any?, Loader<T>> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = loader
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Loader<T>) {
            loader = value
        }
    }
}
