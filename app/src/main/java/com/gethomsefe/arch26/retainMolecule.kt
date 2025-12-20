package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import app.cash.molecule.RecompositionMode
import app.cash.molecule.SnapshotNotifier
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

@Composable
context(scope: CoroutineScope)
fun <T> retainMolecule(
    vararg keys: Any?,
    mode: RecompositionMode = RecompositionMode.ContextClock,
    context: CoroutineContext = scope.coroutineContext,
    body: @Composable () -> T
) = retain(*keys) {
    scope.launchMolecule(mode, context, body =  body)
}
