package com.gethomsefe.arch26.slots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.Loader
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object SlotsView {

    @Composable
    context(scope: CoroutineScope, koin: KoinComponent)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = retain { koin.get<SlotsModel.Presenter>() }
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Pane(modifier: Modifier, state: SlotsModel.State) {
        val isRefreshing = state.slot1 is Loader.Busy
                || state.slot2 is Loader.Busy
                || state.slot3 is Loader.Busy

        PullToRefreshBox(
            modifier = modifier,
            isRefreshing = isRefreshing,
            onRefresh = { state.perform(SlotsModel.Action.Pull) }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SlotItem(state.slot1)
                            SlotItem(state.slot2)
                            SlotItem(state.slot3)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SlotItem(loader: Loader<Char>) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            when (loader) {
                is Loader.Busy -> CircularProgressIndicator()
                is Loader.Success -> Text(
                    text = loader.result.toString(),
                    style = MaterialTheme.typography.displayLarge
                )

                else -> Text(
                    text = "-",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}
