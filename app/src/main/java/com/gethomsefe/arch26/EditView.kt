package com.gethomsefe.arch26

import android.content.ComponentCallbacks
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.android.get

object EditView {
    sealed interface Effect {
        data class Save(val count: Int) : Effect
        data object Cancel : Effect
    }

    @Composable
    context(scope: CoroutineScope, koin: ComponentCallbacks)
    fun Pane(modifier: Modifier, count: Int, produce: (Effect) -> Unit) {
        val present = retain { koin.get<EditModel.Presenter>() }
        val stateFlow = retain(count) {
            scope.launchMolecule(
                mode = RecompositionMode.ContextClock,
                context = AndroidUiDispatcher.Main
            ) {
                present(count)
            }
        }

        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state, produce)
    }

    @Composable
    fun Pane(modifier: Modifier, state: EditModel.State, produce: (Effect) -> Unit) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f))
            Text(text = stringResource(R.string.current_count, state.count))
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { state.perform(EditModel.Action.Decrement) }) {
                    Text(text = stringResource(R.string.decrement))
                }

                Spacer(Modifier.width(8.dp))
                Button(onClick = { state.perform(EditModel.Action.Increment) }) {
                    Text(text = stringResource(R.string.increment))
                }
            }

            Spacer(Modifier.weight(1f))
            Row {
                Button({ produce(Effect.Cancel) }) {
                    Text(stringResource(android.R.string.cancel))
                }

                Spacer(Modifier.width(8.dp))
                Button({ produce(Effect.Save(state.count)) }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}
