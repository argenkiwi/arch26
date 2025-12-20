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
    interface Effects {
        fun save(count: Int)
        fun cancel()
    }

    @Composable
    fun Pane(modifier: Modifier, state: EditModel.State, effects: Effects) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f))
            Text(text = stringResource(R.string.current_count, state.count))
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { state.actions.decrement() }) {
                    Text(text = stringResource(R.string.decrement))
                }

                Spacer(Modifier.width(8.dp))
                Button(onClick = { state.actions.increment() }) {
                    Text(text = stringResource(R.string.increment))
                }
            }

            Spacer(Modifier.weight(1f))
            Row {
                Button({ effects.cancel() }) {
                    Text(stringResource(android.R.string.cancel))
                }

                Spacer(Modifier.width(8.dp))
                Button({ effects.save(state.count) }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}
