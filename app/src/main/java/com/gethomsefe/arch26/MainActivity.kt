package com.gethomsefe.arch26

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import app.cash.molecule.AndroidUiDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Serializable
sealed interface Route {
    data object Display : Route
    data class Edit(val count: Int) : Route
}

class MainActivity : AppCompatActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            context(retain { CoroutineScope(AndroidUiDispatcher.Main) }) {
                Scaffold { paddingValues ->
                    var count by rememberSaveable { mutableIntStateOf(0) }
                    val backStack = remember { mutableStateListOf<Route>(Route.Display) }
                    NavDisplay(
                        backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { route ->
                            when (route) {
                                Route.Display -> NavEntry(route) {
                                    DisplayView.Pane(
                                        count,
                                        Modifier
                                            .fillMaxSize()
                                            .padding(paddingValues)
                                    ) {
                                        when (it) {
                                            DisplayView.Effect.Edit -> Route.Edit(count)
                                                .also(backStack::add)
                                        }
                                    }
                                }

                                is Route.Edit -> NavEntry(route) {
                                    EditView.Pane(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(paddingValues),
                                        EditKoin.state(route.count)
                                    ) { effect ->
                                        when (effect) {
                                            EditView.Effect.Cancel -> backStack.removeLastOrNull()
                                            is EditView.Effect.Save -> {
                                                count = effect.count
                                                backStack.removeLastOrNull()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
