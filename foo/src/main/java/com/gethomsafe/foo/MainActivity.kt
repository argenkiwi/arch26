package com.gethomsafe.foo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import app.cash.molecule.AndroidUiDispatcher
import com.gethomsafe.foo.helloworld.HelloWorldView
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import com.gethomsafe.foo.ui.theme.Arch26Theme

@Serializable
sealed interface Route {
    data object Home : Route
    data object HelloWorld : Route
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Arch26Theme {
                context(retain { CoroutineScope(AndroidUiDispatcher.Main) }) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        val backStack = retain { mutableStateListOf<Route>(Route.HelloWorld) }
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.padding(innerPadding),
                            entryProvider = { route ->
                                when (route) {
                                    Route.Home -> NavEntry(route) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Hello, World!")
                                        }
                                    }
                                    Route.HelloWorld -> NavEntry(route) {
                                        HelloWorldView.Pane(Modifier.fillMaxSize())
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
