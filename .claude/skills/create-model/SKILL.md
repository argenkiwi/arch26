---
name: create-model
description: Creates a new Model file following the project's architecture (object with State, Action/Actions, and a @Factory Presenter). Use this when the user wants to add a new screen's logic or data state.
metadata:
  author: leandro
  version: "1.0"
---

# Create Model

This skill guides the creation of a "Model" which encapsulates the state and logic for a feature or screen in this project.

## Model Structure

A Model should be defined as a Kotlin `object` containing:

1.  **Action/Actions**: A `sealed interface Action` or an `interface Actions` defining user interactions.
2.  **State**: A `data class State` containing all the data needed by the view, including the `perform: (Action) -> Unit` lambda or an `actions: Actions` implementation.
3.  **Presenter**: A `@Factory` annotated class with an `@Composable operator fun invoke(...)` that returns the `State`.

## Steps to Follow

### 1. Define the Requirements
Identify what data the screen needs to display and what actions the user can take.

### 2. Scaffold the Model Object
Create a new file `<FeatureName>Model.kt` in the appropriate package.

```kotlin
package com.gethomsefe.arch26.<feature>

import androidx.compose.runtime.Composable
import org.koin.core.annotation.Factory

object <FeatureName>Model {
    sealed interface Action {
        // Define actions here
    }

    data class State(
        // Define state properties here
        val perform: (Action) -> Unit
    )

    @Factory
    class Presenter {
        @Composable
        operator fun invoke(): State {
            // Manage state using Compose remember/mutableStateOf
            return State(
                perform = { action ->
                    when (action) {
                        // Handle actions
                    }
                }
            )
        }
    }
}
```

### 3. Implementation Details

- **State Management**: Use `androidx.compose.runtime` primitives (`remember`, `mutableStateOf`, `mutableIntStateOf`, etc.) inside the `Presenter.invoke()` function.
- **Asynchronous Work**: If the model needs to load data, use the `rememberLoader` helper if available, or `LaunchedEffect` for side effects.
- **Koin Integration**: Ensure the `Presenter` is annotated with `@Factory`.

## Example (Counter)
```kotlin
object EditModel {
    interface Actions {
        fun increment()
        fun decrement()
    }

    data class State(
        val count: Int,
        val actions: Actions
    )

    @Factory
    class Presenter {
        @Composable
        operator fun invoke(initialCount: Int): State {
            var count by remember { mutableIntStateOf(initialCount) }
            return State(count, object : Actions {
                override fun increment() { count++ }
                override fun decrement() { count-- }
            })
        }
    }
}
```

## Example (Asynchronous Slots)
```kotlin
object SlotsModel {
    sealed interface Action {
        data object Pull : Action
    }

    data class State(
        val slot1: Loader<Char>,
        val perform: (Action) -> Unit
    )

    @Factory
    class Presenter {
        @Composable
        operator fun invoke(): State {
            var slot1 by rememberLoader {
                delay(1000)
                Random.nextInt(65, 91).toChar()
            }

            return State(
                slot1 = slot1,
                perform = {
                    when (it) {
                        Action.Pull -> { slot1 = Busy(Unit) }
                    }
                }
            )
        }
    }
}
```
