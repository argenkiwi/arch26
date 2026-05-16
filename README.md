# Arch26

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-2026.03.00-green.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/platform-Android-lightgrey.svg?logo=android)](https://www.android.com/)

**Arch26** is a modern Android architecture showcase demonstrating a clean, reactive, and composable approach to application development. It leverages a Unidirectional Data Flow (UDF) pattern powered by cutting-edge libraries from the Kotlin ecosystem.

## ✨ Features

- **Counter**: A simple state management example with separate display and edit modes.
- **Slots**: An interactive game demonstrating rapid state updates and UI reactivity.
- **Quakes**: Real-world data integration using the GeoNet API to display recent seismic activity.
- **Todos**: A persistent task manager showcasing local database integration with SQLDelight.
- **Adaptive UI**: Built with Material 3 Adaptive components for a seamless experience across different screen sizes.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for declarative UI components.
- **State Management**: [Molecule](https://github.com/cashapp/molecule) for building reactive state streams using Compose logic.
- **Navigation**: [Navigation3](https://developer.android.com/guide/navigation/navigation-3) for a lightweight, type-safe, and Compose-native navigation experience.
- **DI**: [Koin](https://insert-koin.io/) for pragmatic dependency injection.
- **Networking**: [Ktor](https://ktor.io/) for asynchronous HTTP requests.
- **Persistence**: [SQLDelight](https://github.com/cashapp/sqldelight) for type-safe database operations.
- **Functional Programming**: [Arrow](https://arrow-kt.io/) for robust error handling and functional patterns.

## 🏗 Architecture

The project follows a **Unidirectional Data Flow (UDF)** architecture, ensuring predictable state transitions and high testability.

### Composable Presenters (Molecule)
Logic is encapsulated within `@Composable` Presenter classes. These presenters emit a `State` object that includes both data and interaction callbacks (`Actions`). Molecule converts these presenters into a `StateFlow` consumed by the UI.

> [!TIP]
> This approach allows you to use the full power of Compose (remember, side-effects, etc.) to manage your business logic, keeping it separate from the UI layout.

### Type-Safe Navigation
Navigation is handled via a sealed interface representing routes, integrated with `Navigation3`. This provides a unified way to manage the backstack and handle deep links without the boilerplate of traditional navigation graphs.

### Reactive Persistence
Data from the SQLDelight database is exposed as `Flow` streams, ensuring the UI automatically updates whenever the underlying data changes.

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/gethomsefe/arch26.git
   ```
2. **Open in Android Studio**:
   Open the project folder in the latest version of Android Studio.
3. **Build and Run**:
   Sync Gradle and run the `app` module on an emulator or physical device (API 31+).

> [!IMPORTANT]
> This project requires Kotlin 2.3.20+ and uses the Compose Compiler plugin. Ensure your environment is up to date.
