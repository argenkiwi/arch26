package com.gethomsafe.foo.helloworld

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsafe.foo.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

object HelloWorldView {

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = koinInject<HelloWorldModel.Presenter>()
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @Composable
    fun Pane(modifier: Modifier = Modifier, state: HelloWorldModel.State) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = state.message, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text(text = "Count: ${state.count}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { state.actions.increment() }) {
                Text("Increment")
            }
        }
    }
}
