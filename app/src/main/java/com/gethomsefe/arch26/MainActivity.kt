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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import app.cash.molecule.AndroidUiDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

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
                    var currentCount by rememberSaveable { mutableIntStateOf(0) }
                    val backStack = remember { mutableStateListOf<Route>(Route.Display) }
                    NavDisplay(
                        backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = { route ->
                            when (route) {
                                Route.Display -> NavEntry(route) {
                                    DisplayView.Pane(
                                        currentCount,
                                        Modifier
                                            .fillMaxSize()
                                            .padding(paddingValues)
                                    ) {
                                        when (it) {
                                            DisplayView.Effect.Edit -> Route.Edit(currentCount)
                                                .also(backStack::add)
                                        }
                                    }
                                }

                                is Route.Edit -> NavEntry(route) {
                                    EditView.Pane(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(paddingValues),
                                        state = EditKoin.state(route.count),
                                        effects = object : EditView.Effects {
                                            override fun save(count: Int) {
                                                currentCount = count
                                                backStack.removeLastOrNull()
                                            }

                                            override fun cancel() {
                                                backStack.removeLastOrNull()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
