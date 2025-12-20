package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope

@Composable
context(scope: CoroutineScope)
fun <T> retainMolecule(vararg keys: Any?, body: @Composable () -> T) = retain(*keys) {
    scope.launchMolecule(
        mode = RecompositionMode.ContextClock,
        context = scope.coroutineContext,
        body = body
    )
}
