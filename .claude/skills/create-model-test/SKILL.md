---
name: create-model-test
description: Creates unit tests for a Model's Presenter following the project's Molecule + Turbine testing pattern. Use this when the user wants to add tests for a Model created with the create-model skill.
metadata:
  author: leandro
  version: "1.0"
---

# Create Model Test

This skill guides the creation of unit tests for a Model's `Presenter` in this project. Tests use **Molecule** to drive the composable presenter and **Turbine** to assert on the emitted `State` values.

## Testing Pattern

```
moleculeFlow(RecompositionMode.Immediate) { presenter.invoke(...) }
    .test { awaitItem() / assert }
```

- `moleculeFlow` runs the composable presenter as a `Flow<State>`.
- `RecompositionMode.Immediate` triggers recomposition synchronously on each state change — no delays needed for non-async presenters.
- Turbine's `.test { }` block lets you `awaitItem()` for each emitted state, call actions, and assert in sequence.
- `runTest` from `kotlinx.coroutines.test` is the coroutine test runner — always wrap test bodies in it.
- Instantiate `Presenter()` directly — no Koin, no DI setup required in unit tests.

## Steps to Follow

### 1. Locate the Model

Read the target `<FeatureName>Model.kt` to understand:
- The `State` properties.
- Whether actions use `sealed interface Action` (accessed via `state.perform(Action.Foo)`) or `interface Actions` (accessed via `state.actions.foo()`).
- Whether the presenter has constructor parameters (e.g., `initialCount: Int`).
- Whether the presenter uses `rememberLoader` / `rememberWorker` (async — needs virtual time).

### 2. Determine the Test File Location

Mirror the production source path under the test source set:

```
Production: app/src/main/java/com/gethomsefe/arch26/<package>/<FeatureName>Model.kt
Test:       app/src/test/java/com/gethomsefe/arch26/<package>/<FeatureName>ModelPresenterTest.kt
```

If the model is at the root package (`com.gethomsefe.arch26`), place the test there too.

### 3. Write the Test Class

#### Template — `Actions` interface style (e.g. `EditModel`)

```kotlin
package com.gethomsefe.arch26.<package>

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.gethomsefe.arch26.<package>.<FeatureName>Model
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class <FeatureName>ModelPresenterTest {

    @Test
    fun `initial state is correct`() = runTest {
        val presenter = <FeatureName>Model.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(/* pass initial args if any */)
        }.test {
            with(awaitItem()) {
                // assert initial state properties
                assertEquals(expectedValue, someProperty)
            }
        }
    }

    @Test
    fun `<action name> <expected outcome>`() = runTest {
        val presenter = <FeatureName>Model.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(/* args */)
        }.test {
            with(awaitItem()) {
                // assert before action
                actions.<actionMethod>()
            }
            with(awaitItem()) {
                // assert after action
                assertEquals(expectedValue, someProperty)
            }
        }
    }
}
```

#### Template — `sealed interface Action` / `perform` lambda style (e.g. `SlotsModel`)

```kotlin
package com.gethomsefe.arch26.<package>

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.gethomsefe.arch26.<package>.<FeatureName>Model
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class <FeatureName>ModelPresenterTest {

    @Test
    fun `initial state is correct`() = runTest {
        val presenter = <FeatureName>Model.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                // assert initial state
            }
        }
    }

    @Test
    fun `<action> <expected outcome>`() = runTest {
        val presenter = <FeatureName>Model.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                perform(<FeatureName>Model.Action.<ActionName>)
            }
            with(awaitItem()) {
                // assert changed state
            }
        }
    }
}
```

### 4. Handle Async Presenters (`rememberLoader` / `Worker`)

When the presenter uses `rememberLoader`, `rememberWorker`, or any `delay`-based logic, the state machine emits multiple items over virtual time. Use `advanceTimeBy` or `advanceUntilIdle` inside the `test { }` block:

```kotlin
import kotlinx.coroutines.test.advanceTimeBy

@Test
fun `slot completes after delay`() = runTest {
    val presenter = SlotsModel.Presenter()
    moleculeFlow(mode = RecompositionMode.Immediate) {
        presenter.invoke()
    }.test {
        // Initial: all Idle
        awaitItem() // Idle state

        // Trigger action
        awaitItem().perform(SlotsModel.Action.Pull)

        // After pull: all Busy
        with(awaitItem()) {
            assertTrue(slot1 is Busy)
        }

        // Advance past first delay (1000ms)
        advanceTimeBy(1001)
        with(awaitItem()) {
            assertTrue(slot1 is Done)
            assertTrue(slot2 is Busy)
        }

        // Advance past second delay (500ms more = 1500ms total)
        advanceTimeBy(500)
        with(awaitItem()) {
            assertTrue(slot2 is Done)
        }
    }
}
```

Key rule: with `RecompositionMode.Immediate`, each `mutableStateOf` write causes a new emission — one `awaitItem()` per state transition.

### 5. What Tests to Write

For every Model, write at minimum:

1. **Initial state** — verify all `State` properties after `presenter.invoke()` with default/given args.
2. **One test per meaningful action** — trigger the action, `awaitItem()`, assert the changed property.
3. **Async completions** (if applicable) — verify `Idle → Busy → Done` transitions using virtual time.

Avoid testing implementation details (e.g., internal `remember` variables). Test only the public `State` surface.

### 6. Required Imports Cheatsheet

| Symbol | Import |
|---|---|
| `moleculeFlow` | `app.cash.molecule.moleculeFlow` |
| `RecompositionMode` | `app.cash.molecule.RecompositionMode` |
| `test { }` | `app.cash.turbine.test` |
| `runTest` | `kotlinx.coroutines.test.runTest` |
| `advanceTimeBy` | `kotlinx.coroutines.test.advanceTimeBy` |
| `Busy`, `Done`, `Idle` | `com.gethomsefe.arch26.Busy` / `.Done` / `.Idle` |
| `assertEquals` | `org.junit.Assert.assertEquals` |
| `assertTrue` | `org.junit.Assert.assertTrue` |

## Full Example — `EditModel`

```kotlin
package com.gethomsefe.arch26

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.gethomsefe.arch26.counter.edit.EditModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EditModelPresenterTest {

    @Test
    fun `initial state is correct`() = runTest {
        val presenter = EditModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(initialCount = 5)
        }.test {
            with(awaitItem()) {
                assertEquals(5, count)
            }
        }
    }

    @Test
    fun `increment increases count`() = runTest {
        val presenter = EditModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(initialCount = 0)
        }.test {
            with(awaitItem()) {
                assertEquals(0, count)
                actions.increment()
            }
            with(awaitItem()) {
                assertEquals(1, count)
                actions.increment()
            }
            with(awaitItem()) {
                assertEquals(2, count)
            }
        }
    }

    @Test
    fun `decrement decreases count`() = runTest {
        val presenter = EditModel.Presenter()
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke(initialCount = 10)
        }.test {
            with(awaitItem()) {
                assertEquals(10, count)
                actions.decrement()
            }
            with(awaitItem()) {
                assertEquals(9, count)
                actions.decrement()
            }
            with(awaitItem()) {
                assertEquals(8, count)
            }
        }
    }
}
```

## Run the Tests

```bash
./gradlew test --tests "*.<FeatureName>ModelPresenterTest"
```
