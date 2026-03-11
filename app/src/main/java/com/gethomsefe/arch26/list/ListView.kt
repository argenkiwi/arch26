package com.gethomsefe.arch26.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object ListView {
    sealed interface Effect {
        data object OnShowCounter : Effect
        data object OnShowSlots : Effect
    }

    @Composable
    fun Pane(modifier: Modifier = Modifier, produce: (Effect) -> Unit) {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            item {
                ListItem(
                    headlineContent = { Text("Counter Example") },
                    modifier = Modifier.clickable { produce(Effect.OnShowCounter) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Slots Example") },
                    modifier = Modifier.clickable { produce(Effect.OnShowSlots) }
                )
            }
        }
    }
}
