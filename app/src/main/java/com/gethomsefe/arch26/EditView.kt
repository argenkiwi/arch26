package com.gethomsefe.arch26

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

object EditView {
    sealed interface Effect {
        data class Save(val count: Int) : Effect
        data object Cancel : Effect
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
