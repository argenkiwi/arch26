---
name: create-view
description: Creates a new View file for a Model following the project's Compose + Molecule architecture (object with two Pane overloads — a connected one that injects the Presenter, and a stateless one that takes State directly). Optionally wires it into MainActivity navigation and the ListView menu. Use this when the user wants to add a screen UI for an existing or new Model.
metadata:
  author: leandro
  version: "1.0"
---

# Create View

This skill guides the creation of a `<FeatureName>View` Compose screen that connects to a `<FeatureName>Model.Presenter` in this project.

## View Structure

A View is a Kotlin `object` containing:

1. **`Effects` / `Effect`** (optional): A `sealed interface Effect` or `interface Effects` for one-shot outbound events — navigation, returning a value, etc. Only needed when the screen communicates back to the caller.
2. **Connected `Pane`**: A `@Composable` function with `context(scope: CoroutineScope)` that injects the presenter via Koin, creates the retained `StateFlow` via `retainMolecule`, collects it, and delegates to the stateless Pane.
3. **Stateless `Pane`**: A `@Composable` function that takes `state: <FeatureName>Model.State` directly. This is the function that contains the actual layout logic and is independently previewable/testable.

```
Connected Pane  →  retainMolecule { presenter.invoke(...) }  →  Stateless Pane(state)
```

## Steps to Follow

### 1. Read the Model

Read `<FeatureName>Model.kt` to understand:
- The `State` properties.
- Whether actions use `sealed interface Action` (`state.perform(Action.Foo)`) or `interface Actions` (`state.actions.foo()`).
- The `Presenter.invoke(...)` signature — note any parameters (e.g., `initialCount: Int`).

### 2. Decide on Effects

Does this screen need to communicate outward (navigate away, return data)?
- **Yes** → define a `sealed interface Effect` inside the view object and add an `effects: Effects` / `produce: (Effect) -> Unit` parameter to both Pane overloads.
- **No** → omit effects entirely (e.g., `SlotsView`, `QuakesView`, `TodosView`).

Use `interface Effects` (with named methods) when there are multiple effect variants with data. Use `(Effect) -> Unit` lambda when effects are simple sealed objects.

### 3. Create the View File

Create `<FeatureName>View.kt` in the same package as the model.

#### Template — no effects, no presenter args

```kotlin
package com.gethomsefe.arch26.<package>

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

object <FeatureName>View {

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = koinInject<<FeatureName>Model.Presenter>()
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @Composable
    fun Pane(modifier: Modifier, state: <FeatureName>Model.State) {
        // Layout using state properties
        // Dispatch actions via state.perform(Action.Foo) or state.actions.foo()
    }
}
```

#### Template — with effects (lambda style) and presenter args

```kotlin
package com.gethomsefe.arch26.<package>

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

object <FeatureName>View {
    sealed interface Effect {
        // e.g. data object GoBack : Effect
        // e.g. data class Save(val value: Int) : Effect
    }

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier, initialArg: ArgType, produce: (Effect) -> Unit) {
        val presenter = koinInject<<FeatureName>Model.Presenter>()
        val stateFlow = retainMolecule(initialArg) { presenter.invoke(initialArg) }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state, produce)
    }

    @Composable
    fun Pane(modifier: Modifier, state: <FeatureName>Model.State, produce: (Effect) -> Unit) {
        // Layout using state properties
        // Fire effects: produce(Effect.GoBack)
    }
}
```

#### Template — with effects (interface style)

```kotlin
object <FeatureName>View {
    interface Effects {
        fun save(value: Int)
        fun cancel()
    }

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier, initialArg: ArgType, effects: Effects) {
        val presenter = koinInject<<FeatureName>Model.Presenter>()
        val stateFlow = retainMolecule(initialArg) { presenter.invoke(initialArg) }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state, effects)
    }

    @Composable
    fun Pane(modifier: Modifier, state: <FeatureName>Model.State, effects: Effects) {
        // Layout using state; call effects.save(state.value) etc.
    }
}
```

### 4. Implement the Stateless Pane

Guidelines for the layout:
- Use Material3 components (`Column`, `LazyColumn`, `ListItem`, `Button`, `Text`, etc.).
- For `Worker`/`Loader` state, use `when (state.someWorker)` to render `Busy → CircularProgressIndicator`, `Done → content`, `Idle/else → Unit`.
- For lists, use `LazyColumn` with `items(state.list, key = { it.id })`.
- For pull-to-refresh, wrap with `PullToRefreshBox` using `isRefreshing = state.worker is Busy` and `onRefresh = { state.perform(Action.Refresh) }`. Requires `@OptIn(ExperimentalMaterial3Api::class)`.
- Local UI-only state (e.g., text field input) that does NOT belong in the model can live in the stateless Pane using `remember { mutableStateOf(...) }`.

### 5. Register the Route in MainActivity (if this is a new screen)

Open `MainActivity.kt` and:

**a) Add a route to the `Route` sealed interface:**
```kotlin
@Serializable
sealed interface Route {
    // ...existing routes...
    data object <FeatureName> : Route          // no args
    data class <FeatureName>(val id: Int) : Route  // with args
}
```

**b) Add a `NavEntry` in the `entryProvider` `when` block:**
```kotlin
Route.<FeatureName> -> NavEntry(route) {
    <FeatureName>View.Pane(Modifier.fillMaxSize())
}
```

For views with effects, wire navigation in the effects handler:
```kotlin
Route.<FeatureName> -> NavEntry(route) {
    <FeatureName>View.Pane(
        modifier = Modifier.fillMaxSize(),
        produce = { effect ->
            when (effect) {
                <FeatureName>View.Effect.GoBack -> backStack.removeLastOrNull()
            }
        }
    )
}
```

### 6. Add to the Menu (optional)

If the feature should appear in the app's home list, make two edits in `ListView.kt` and `MainActivity.kt`:

**ListView.kt** — add an Effect and a list item:
```kotlin
sealed interface Effect {
    // ...existing effects...
    data object OnShow<FeatureName> : Effect
}

// Inside Pane:
item {
    ListItem(
        headlineContent = { Text("<Feature Display Name>") },
        modifier = Modifier.clickable { produce(Effect.OnShow<FeatureName>) }
    )
}
```

**MainActivity.kt** — handle the new effect in the `ListView` NavEntry:
```kotlin
Route.List -> NavEntry(route) {
    ListView.Pane {
        when (it) {
            // ...existing cases...
            ListView.Effect.OnShow<FeatureName> -> backStack.add(Route.<FeatureName>)
        }
    }
}
```

## Key Rules

- **Never** put navigation logic or `koinInject` in the stateless Pane — it must remain pure.
- **Always** pass `retainMolecule` keys matching the presenter args so the flow is recreated when args change (e.g., `retainMolecule(count) { presenter.invoke(count) }`).
- `context(scope: CoroutineScope)` is a Kotlin context parameter — the `CoroutineScope` is provided by the `retain { CoroutineScope(AndroidUiDispatcher.Main) }` block in `MainActivity`.
- `@OptIn(ExperimentalMaterial3Api::class)` is required on functions that use `PullToRefreshBox`.

## Full Example — SlotsView

```kotlin
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.Busy
import com.gethomsefe.arch26.Done
import com.gethomsefe.arch26.Idle
import com.gethomsefe.arch26.Loader
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

object SlotsView {

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = koinInject<SlotsModel.Presenter>()
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Pane(modifier: Modifier, state: SlotsModel.State) {
        val isRefreshing = state.slot1 is Busy || state.slot2 is Busy || state.slot3 is Busy
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
                is Busy -> CircularProgressIndicator()
                is Done -> Text(text = loader.result.toString(), style = MaterialTheme.typography.displayLarge)
                Idle -> Text(text = "-", style = MaterialTheme.typography.displayLarge)
            }
        }
    }
}
```
