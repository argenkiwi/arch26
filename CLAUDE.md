# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew build                   # Full build
./gradlew assembleDebug           # Debug APK only
./gradlew test                    # Unit tests
./gradlew test --tests "*.EditModelPresenterTest"  # Single test class
./gradlew connectedAndroidTest    # Instrumented tests
./gradlew lint                    # Lint checks
```

## Architecture Overview

This is a single-module Android app (`app/`) demonstrating a modern, ViewModel-free architecture.

### Core Pattern: Presenter + Molecule

Instead of ViewModels, this codebase uses **composable Presenter functions** that return a `State` object containing both data and action callbacks. [Molecule](https://github.com/cashapp/molecule) converts these composable presenters into `StateFlow`.

```
User Event → State.actions callback → Presenter recomposes → New State → UI
```

- `retainMolecule.kt` — bridges `launchMolecule()` with `retain {}` so the presenter scope survives configuration changes
- Each feature has a `Presenter` composable returning a `State` data class with a `perform` lambda or nested `Actions`

### Navigation

Uses **Navigation3** (`androidx.navigation3`) with `@Serializable` sealed interfaces as routes. The backstack is a `SnapshotStateList<Route>`. Routes are defined in `MainActivity.kt` and each route maps to a `NavDisplay` entry.

### Dependency Injection

**Koin** with the compiler plugin — uses annotations (`@Module`, `@Factory`, `@Single`, `@ComponentScan`) for compile-time code generation. Modules are picked up via `@ComponentScan` in `Arch26.kt`.

### Async Work: Worker

`Worker<I, O>` is a sealed interface with three states: `Idle`, `Busy<I>`, `Done<O>`. Helpers `rememberWorker()`, `rememberRunner()`, `rememberLoader()`, `rememberSaver()` wrap coroutine launches and expose the state to Compose.

### Key Libraries

| Concern | Library |
|---|---|
| Reactive presenters | Molecule 2.2.0 |
| Navigation | Navigation3 1.0.1 |
| DI | Koin 4.2.0 + compiler |
| Networking | Ktor 3.4.1 |
| Database | SQLDelight 2.3.2 |
| Error handling | Arrow Core 2.2.2.1 (`Either`) |
| Flow testing | Turbine 1.2.1 |

### Testing Presenters

Presenters are tested by running them inside `moleculeFlow { }` and asserting on emitted states with Turbine:

```kotlin
moleculeFlow(RecompositionMode.Immediate) {
    EditModel.Presenter(...)
}.test {
    val state = awaitItem()
    // assert on state
}
```

## Target SDK

- `minSdk` = 31, `compileSdk` = 36, Java 17
