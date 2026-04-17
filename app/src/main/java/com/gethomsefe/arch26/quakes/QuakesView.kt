package com.gethomsefe.arch26.quakes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.Busy
import com.gethomsefe.arch26.Done
import com.gethomsefe.arch26.quakes.data.Quake
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

private val mmiLevels = listOf(3, 4, 5, 6, 7)

object QuakesView {

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = koinInject<QuakesModel.Presenter>()
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Pane(modifier: Modifier, state: QuakesModel.State) {
        val isRefreshing = state.worker is Busy
        val error = (state.worker as? Done)?.result?.fold({ it }, { null })

        Column(modifier = modifier) {
            MmiFilter(
                selected = state.mmi,
                onSelect = { state.perform(QuakesModel.Action.SetMmi(it)) }
            )

            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = isRefreshing,
                onRefresh = { state.perform(QuakesModel.Action.Refresh) }
            ) {
                QuakeList(
                    quakes = state.quakes,
                    error = error
                )
            }
        }
    }

    @Composable
    private fun MmiFilter(selected: Int, onSelect: (Int) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            mmiLevels.forEach { mmi ->
                FilterChip(
                    selected = mmi == selected,
                    onClick = { onSelect(mmi) },
                    label = { Text("MMI $mmi") }
                )
            }
        }
    }

    @Composable
    private fun QuakeList(quakes: List<Quake>, error: String? = null) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (quakes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (error != null) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "No quakes found",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            } else {
                items(quakes, key = { it.id }) { quake ->
                    ListItem(
                        headlineContent = { Text(quake.locality) },
                        supportingContent = {
                            Text("M${quake.magnitude}  •  ${quake.depth} km deep  •  MMI ${quake.mmi}")
                        },
                        trailingContent = {
                            Text(
                                text = "M${quake.magnitude}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    )
                }
            }
        }
    }
}
