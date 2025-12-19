package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@Composable
context(scope: CoroutineScope)
inline fun <reified P, T> retainMolecule(
    noinline presenter: () -> P,
    crossinline present: @Composable (P) -> T,
    vararg keys: Any?
): StateFlow<T> {
    val presenter = retain(presenter)
    return retain(*keys) {
        scope.launchMolecule(
            mode = RecompositionMode.ContextClock,
            context = scope.coroutineContext
        ) {
            present(presenter)
        }
    }
}
