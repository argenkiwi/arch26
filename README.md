# Arch26

A modern Android architecture sample demonstrating a clean, reactive, and composable approach to app development.

## Architecture Overview

This project follows a Unidirectional Data Flow (UDF) pattern, leveraging modern libraries to ensure a highly decoupled and testable codebase.

### Dependency Injection (DI)
The project uses **[Koin](https://insert-koin.io/)** for dependency injection.
- **Setup:** Koin is initialized in the `Arch26` Application class.
- **Modules:** Dependencies are organized into modules (e.g., `EditModule.kt`).
- **Integration:** Components like `MainActivity` and `EditView` use Koin to retrieve their required dependencies, often using the `KoinComponent` interface.

### Navigation
Navigation is handled using **Navigation3**, a lightweight and Compose-native navigation library.
- **Routes:** Navigation destinations are defined as a `@Serializable` sealed interface (`Route`).
- **Backstack:** The application state manages a `SnapshotStateList<Route>` as its backstack. Navigation is performed by simply adding or removing items from this list.
- **Display:** `NavDisplay` is used in `MainActivity` to map the current `Route` to a `NavEntry`, which contains the corresponding UI component.

### State Management
State management is powered by **[Molecule](https://github.com/cashapp/molecule)** and a **Presenter** pattern.
- **Presenter:** Logic is encapsulated in `Presenter` classes (e.g., `EditModel.Presenter`). These presenters are `@Composable` functions that return a `State` object containing both data and interaction callbacks (`Actions`).
- **Molecule:** The `retainMolecule` helper (in `retainMolecule.kt`) converts these `@Composable` presenters into a `StateFlow`, allowing the UI to reactively observe state changes.
- **Persistence:** The project utilizes `androidx.compose.runtime.retain` to preserve the `CoroutineScope` and other objects across configuration changes, providing a more flexible alternative to traditional `ViewModel`s.

## Key Components
- **`MainActivity.kt`**: The entry point, managing the navigation backstack and root UI structure.
- **`EditModel.kt`**: Defines the State, Actions, and Presenter for the editing screen.
- **`EditView.kt`**: The UI implementation for the editing screen, consuming state from the presenter.
- **`retainMolecule.kt`**: A utility providing a bridge between Molecule presenters and Compose-native retention.
