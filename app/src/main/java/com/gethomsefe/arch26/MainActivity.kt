package com.gethomsefe.arch26

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import app.cash.molecule.AndroidUiDispatcher
import com.gethomsefe.arch26.counter.display.DisplayView
import com.gethomsefe.arch26.counter.edit.EditView
import com.gethomsefe.arch26.list.ListView
import com.gethomsefe.arch26.slots.SlotsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    data object List : Route
    data object Display : Route
    data class Edit(val count: Int) : Route
    data object Slots : Route
}

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            context(retain { CoroutineScope(AndroidUiDispatcher.Main) }) {
                Scaffold { paddingValues ->
                    val backStack = retain { mutableStateListOf<Route>(Route.List) }
                    var currentCount by rememberSaveable { mutableIntStateOf(0) }
                    NavDisplay(
                        backStack,
                        onBack = { backStack.removeLastOrNull() },
                        sceneStrategy = rememberListDetailSceneStrategy(),
                        modifier = Modifier.padding(paddingValues),
                        entryProvider = { route ->
                            when (route) {
                                Route.List -> NavEntry(
                                    route,
                                    metadata = ListDetailSceneStrategy.listPane()
                                ) {
                                    ListView.Pane {
                                        when (it) {
                                            ListView.Effect.OnShowCounter -> backStack.add(Route.Display)
                                            ListView.Effect.OnShowSlots -> backStack.add(Route.Slots)
                                        }
                                    }
                                }

                                Route.Display -> NavEntry(
                                    route,
                                    metadata = ListDetailSceneStrategy.detailPane()
                                ) {
                                    DisplayView.Pane(Modifier.fillMaxSize(), currentCount) {
                                        when (it) {
                                            DisplayView.Effect.Edit -> Route.Edit(currentCount)
                                                .also(backStack::add)
                                        }
                                    }
                                }

                                is Route.Edit -> NavEntry(
                                    route,
                                    metadata = ListDetailSceneStrategy.detailPane()
                                ) {
                                    EditView.Pane(
                                        Modifier.fillMaxSize(),
                                        count = route.count,
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

                                Route.Slots -> NavEntry(
                                    route,
                                    metadata = ListDetailSceneStrategy.detailPane()
                                ) {
                                    SlotsView.Pane(Modifier.fillMaxSize())
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
