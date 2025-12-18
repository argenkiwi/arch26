package com.gethomsefe.arch26

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

object DisplayView {
    sealed interface Effect {
        data object Edit : Effect
    }

    @Composable
    fun Pane(count: Int, modifier: Modifier = Modifier, produce: (Effect) -> Unit) {
        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.current_count, count))
            Spacer(Modifier.height(8.dp))
            Button({ produce(Effect.Edit) }) {
                Text(stringResource(R.string.edit))
            }
        }
    }
}
