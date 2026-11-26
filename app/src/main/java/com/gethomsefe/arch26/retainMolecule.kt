package com.gethomsefe.arch26

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.retain
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.currentKoinScope
import kotlin.coroutines.CoroutineContext

/**
 * Launches [body] as a Molecule in a child of [scope] that survives configuration changes.
 * The child scope is cancelled once the retained value is retired (e.g. its nav entry is popped),
 * so presenters don't keep running after their screen is gone.
 */
@Composable
context(scope: CoroutineScope)
fun <T> retainMolecule(
    vararg keys: Any?,
    mode: RecompositionMode = RecompositionMode.ContextClock,
    context: CoroutineContext = scope.coroutineContext,
    body: @Composable () -> T
): StateFlow<T> = retain(*keys) {
    RetainedMolecule(
        CoroutineScope(context + SupervisorJob(scope.coroutineContext[Job])),
        mode,
        body
    )
}.stateFlow

private class RetainedMolecule<T>(
    private val scope: CoroutineScope,
    mode: RecompositionMode,
    body: @Composable () -> T
) : RetainObserver {
    val stateFlow: StateFlow<T> = scope.launchMolecule(mode, body = body)

    override fun onRetained() = Unit
    override fun onEnteredComposition() = Unit
    override fun onExitedComposition() = Unit
    override fun onRetired() = scope.cancel()
    override fun onUnused() = scope.cancel()
}

/**
 * Resolves [P] from the current Koin scope once and retains it.
 * Unlike `koinInject()`, a `factory` is not re-resolved on every recomposition.
 */
@Composable
inline fun <reified P : Any> retainPresenter(vararg keys: Any?): P {
    val koinScope = currentKoinScope()
    return retain(*keys) { koinScope.get<P>() }
}
