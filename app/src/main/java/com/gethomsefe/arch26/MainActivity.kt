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
import com.gethomsefe.arch26.quakes.QuakesView
import com.gethomsefe.arch26.slots.SlotsView
import com.gethomsefe.arch26.todo.TodosView
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    data object List : Route
    data object Display : Route
    data class Edit(val count: Int) : Route
    data object Slots : Route
    data object Quakes : Route
    data object Todos : Route
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            context(retain { CoroutineScope(AndroidUiDispatcher.Main) }) {
                Scaffold { paddingValues ->
                    val backStack = retain { mutableStateListOf<Route>(Route.List) }
                    var currentCount by rememberSaveable { mutableIntStateOf(0) }
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        modifier = Modifier.padding(paddingValues),
                        entryProvider = { route ->
                            when (route) {
                                Route.List -> NavEntry(route) {
                                    ListView.Pane {
                                        when (it) {
                                            ListView.Effect.OnShowCounter -> backStack.add(Route.Display)
                                            ListView.Effect.OnShowSlots -> backStack.add(Route.Slots)
                                            ListView.Effect.OnShowQuakes -> backStack.add(Route.Quakes)
                                            ListView.Effect.OnShowTodos -> backStack.add(Route.Todos)
                                        }
                                    }
                                }

                                Route.Display -> NavEntry(route) {
                                    DisplayView.Pane(Modifier.fillMaxSize(), currentCount) {
                                        when (it) {
                                            DisplayView.Effect.Edit -> Route.Edit(currentCount)
                                                .also(backStack::add)
                                        }
                                    }
                                }

                                is Route.Edit -> NavEntry(route) {
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

                                Route.Slots -> NavEntry(route) {
                                    SlotsView.Pane(Modifier.fillMaxSize())
                                }

                                Route.Quakes -> NavEntry(route) {
                                    QuakesView.Pane(Modifier.fillMaxSize())
                                }

                                Route.Todos -> NavEntry(route) {
                                    TodosView.Pane(Modifier.fillMaxSize())
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
