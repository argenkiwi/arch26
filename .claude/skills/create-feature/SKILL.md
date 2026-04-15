---
name: create-feature
description: Scaffolds a complete feature package — Model, View, and Presenter unit tests — following the project's Molecule + Koin architecture. Orchestrates the create-model, create-view, and create-model-test skills. Creates a module file only when third-party dependencies are needed. Use this when the user wants to add a new screen end-to-end.
metadata:
  author: leandro
  version: "1.0"
---

# Create Feature

This skill scaffolds all the files needed for a new feature screen in one pass:

| File | Skill reference |
|---|---|
| `<FeatureName>Model.kt` | [create-model](../create-model/SKILL.md) |
| `<FeatureName>Module.kt` *(optional)* | this skill — only when third-party deps are needed |
| `<FeatureName>View.kt` | [create-view](../create-view/SKILL.md) |
| `<FeatureName>ModelPresenterTest.kt` | [create-model-test](../create-model-test/SKILL.md) |

Navigation wiring in `MainActivity.kt` and the home-screen menu entry in `ListView.kt` are also handled here.

---

## Step 1 — Gather Requirements

Before writing any code, clarify:

1. **Feature name** — e.g., `Weather`, `Profile`, `Settings`
2. **Package** — typically `com.gethomsefe.arch26.<featurename>` (lowercase, no spaces)
3. **State & actions** — what data does the screen display? What can the user do?
4. **Async dependencies** — does the presenter need to load data (network, DB)? If so, what?
5. **Effects / navigation** — does the screen navigate away or return a value to the caller?
6. **Menu entry** — should it appear in the home list (`ListView`)?

---

## Step 2 — Create the Model

Follow the **create-model** skill in full.

Key points:
- Create `app/src/main/java/com/gethomsefe/arch26/<feature>/<FeatureName>Model.kt`
- The `Presenter` class must be annotated with `@Factory` — this is what Koin's `@ComponentScan` picks up.
- Use `sealed interface Action` + `perform` lambda, or `interface Actions`, depending on complexity.

---

## Step 3 — Create a Module File (only if needed)

The root `Arch26Module` carries `@ComponentScan`, which auto-discovers every `@Factory`, `@Single`, and `@Singleton` annotation in the entire `com.gethomsefe.arch26` package tree. **No per-feature `@Module` class is needed.**

### Presenter only (most common)

Nothing to do — the `@Factory Presenter` in the Model is discovered automatically.

### Third-party dependencies (network / database / external SDK)

When the feature needs objects that can't carry Koin annotations directly (e.g., an `HttpClient`, a `Database`), create `<FeatureName>Module.kt` with plain top-level annotated functions. **No `@Module` class required.**

```kotlin
package com.gethomsefe.arch26.<feature>

import org.koin.core.annotation.Singleton

@Singleton
fun someClient(): SomeClient = SomeClient(...)

@Singleton
fun someOtherDep(client: SomeClient): SomeDep = SomeDep(client)
```

**No changes to `Arch26.kt` are needed** — the root `@ComponentScan` picks up annotated functions in any subpackage.

### Annotation quick-reference

| Annotation | When to use |
|---|---|
| `@Factory` | Short-lived dependency, new instance per injection |
| `@Single` / `@Singleton` | Shared, long-lived dependency |

---

## Step 4 — Create the View

Follow the **create-view** skill in full.

Key points:
- Create `app/src/main/java/com/gethomsefe/arch26/<feature>/<FeatureName>View.kt`
- Two `Pane` overloads: connected (with `context(scope: CoroutineScope)`) + stateless (takes `State` directly).
- Define a `sealed interface Effect` inside the view object only if navigation or outbound events are needed.
- `retainMolecule` keys must match any presenter constructor arguments.

---

## Step 5 — Wire Navigation in MainActivity

Open `MainActivity.kt` (`app/src/main/java/com/gethomsefe/arch26/MainActivity.kt`).

### a) Add a Route

```kotlin
@Serializable
sealed interface Route {
    // ...existing routes...
    data object <FeatureName> : Route          // no args
    // OR:
    data class <FeatureName>(val id: Int) : Route  // with args
}
```

### b) Add a NavEntry in entryProvider

```kotlin
Route.<FeatureName> -> NavEntry(route) {
    <FeatureName>View.Pane(Modifier.fillMaxSize())
}
```

With effects:
```kotlin
Route.<FeatureName> -> NavEntry(route) {
    <FeatureName>View.Pane(
        modifier = Modifier.fillMaxSize(),
        produce = { effect ->
            when (effect) {
                <FeatureName>View.Effect.GoBack -> backStack.removeLastOrNull()
                // other effects...
            }
        }
    )
}
```

---

## Step 6 — Add to the Home Menu (if requested)

**`ListView.kt`** — add an Effect case and a list item:

```kotlin
sealed interface Effect {
    // ...existing...
    data object OnShow<FeatureName> : Effect
}

// Inside Pane LazyColumn:
item {
    ListItem(
        headlineContent = { Text("<Feature Display Name>") },
        modifier = Modifier.clickable { produce(Effect.OnShow<FeatureName>) }
    )
}
```

**`MainActivity.kt`** — handle the new effect in the `Route.List` NavEntry:

```kotlin
Route.List -> NavEntry(route) {
    ListView.Pane {
        when (it) {
            // ...existing...
            ListView.Effect.OnShow<FeatureName> -> backStack.add(Route.<FeatureName>)
        }
    }
}
```

---

## Step 7 — Create the Tests

Follow the **create-model-test** skill in full.

Key points:
- Create `app/src/test/java/com/gethomsefe/arch26/<feature>/<FeatureName>ModelPresenterTest.kt`
- Instantiate `Presenter()` directly — no Koin needed.
- Use `moleculeFlow(RecompositionMode.Immediate)` + Turbine.
- For async presenters (`rememberLoader`/`rememberWorker`), use `advanceTimeBy` with `@OptIn(ExperimentalCoroutinesApi::class)`.
- Cover: initial state, one test per meaningful action, async state transitions if applicable.

Run the tests after writing:
```bash
./gradlew :app:testDebugUnitTest --tests "*.<FeatureName>ModelPresenterTest"
```

---

## Checklist

Work through each item in order. Do not move to the next until the current file compiles.

- [ ] `<FeatureName>Model.kt` — object with `State`, `Action`/`Actions`, `@Factory Presenter`
- [ ] `<FeatureName>Module.kt` — only if third-party deps needed; top-level annotated functions, no `@Module` class
- [ ] `<FeatureName>View.kt` — two `Pane` overloads, optional `Effect`
- [ ] `MainActivity.kt` — new `Route` entry + `NavEntry` in `entryProvider`
- [ ] `ListView.kt` — new `Effect` + list item (if menu entry requested)
- [ ] `<FeatureName>ModelPresenterTest.kt` — initial state + per-action tests
- [ ] Tests pass: `./gradlew :app:testDebugUnitTest --tests "*.<FeatureName>ModelPresenterTest"`
- [ ] App builds: `./gradlew assembleDebug`

---

## Complete Example — "Weather" feature (sketch)

**Package**: `com.gethomsefe.arch26.weather`

**Files to create**:
```
app/src/main/java/com/gethomsefe/arch26/weather/
    WeatherModel.kt
    WeatherView.kt
app/src/test/java/com/gethomsefe/arch26/weather/
    WeatherModelPresenterTest.kt
```

No module file needed — `WeatherModel.Presenter` is `@Factory` and is discovered by the root `@ComponentScan`.

**`WeatherModel.kt`** (loads current conditions, lets user refresh):
```kotlin
package com.gethomsefe.arch26.weather

import androidx.compose.runtime.Composable
import com.gethomsefe.arch26.Busy
import com.gethomsefe.arch26.Loader
import com.gethomsefe.arch26.rememberLoader
import org.koin.core.annotation.Factory

object WeatherModel {
    sealed interface Action {
        data object Refresh : Action
    }

    data class State(
        val conditions: Loader<String>,
        val perform: (Action) -> Unit
    )

    @Factory
    class Presenter {
        @Composable
        operator fun invoke(): State {
            var conditions by rememberLoader { fetchConditions() }
            return State(
                conditions = conditions,
                perform = { when (it) { Action.Refresh -> { conditions = Busy(Unit) } } }
            )
        }

        private suspend fun fetchConditions(): String = "Sunny, 22°C"
    }
}
```

**`WeatherView.kt`** (sketch — stateless Pane omitted for brevity):
```kotlin
package com.gethomsefe.arch26.weather

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

object WeatherView {

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = koinInject<WeatherModel.Presenter>()
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @Composable
    fun Pane(modifier: Modifier, state: WeatherModel.State) {
        // render state.conditions (Idle/Busy/Done)
    }
}
```

**Route + NavEntry in `MainActivity.kt`**:
```kotlin
// In Route:
data object Weather : Route

// In entryProvider:
Route.Weather -> NavEntry(route) {
    WeatherView.Pane(Modifier.fillMaxSize())
}
```

**ListView entry**:
```kotlin
// ListView.Effect:
data object OnShowWeather : Effect

// ListView.Pane item:
item {
    ListItem(
        headlineContent = { Text("Weather") },
        modifier = Modifier.clickable { produce(Effect.OnShowWeather) }
    )
}

// MainActivity Route.List handler:
ListView.Effect.OnShowWeather -> backStack.add(Route.Weather)
```
