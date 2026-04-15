---
name: init-app
description: Migrates an existing Android application module to the Molecule + Koin + Navigation3 architecture used in this project, so the create-feature skill can be used to add features. Updates build.gradle.kts (plugins, Java 17, dependencies), creates the Application class, retainMolecule.kt, and rewrites MainActivity with the Navigation3 scaffold. Use this when porting an app from any other architecture.
metadata:
  author: leandro
  version: "1.0"
---

# Init App

This skill migrates an existing Android application module to the project's Molecule + Koin + Navigation3 architecture so that the **create-feature** skill can be run against it.

## Before You Start

Read the following files in the target module to understand what already exists:

1. `<module>/build.gradle.kts` — current plugins, Java version, dependencies
2. `<module>/src/main/AndroidManifest.xml` — current `<application>` element
3. `<module>/src/main/java/<pkg>/MainActivity.kt` — current Activity class
4. `gradle/libs.versions.toml` (root) — confirm all required aliases exist

The target module's **package name** (from `namespace` / `applicationId` in `build.gradle.kts`) is needed for every file you create.

---

## Step 1 — Update build.gradle.kts

### 1a. Add plugins

Add two plugins that are likely missing:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.koin.compiler)           // ADD
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)  // ADD
}
```

### 1b. Upgrade Java version and add compiler flag

Replace the existing `compileOptions` / `kotlin` blocks:

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

Add (or replace) the top-level `kotlin` block:

```kotlin
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
```

Add the import at the top of the file:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
```

### 1c. Add testOptions

Inside the `android { }` block:

```kotlin
testOptions {
    unitTests.isReturnDefaultValues = true
}
```

### 1d. Remove targetSdk from defaultConfig (optional but consistent)

The `app` module omits `targetSdk` from `defaultConfig`. Remove it if present:

```kotlin
defaultConfig {
    applicationId = "<pkg>"
    minSdk = 31
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    // remove: targetSdk = XX
}
```

### 1e. Replace the dependencies block

Keep existing Compose/UI dependencies, remove redundant ones, and add the architecture dependencies. The final block should look like:

```kotlin
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Koin DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)

    // Molecule
    implementation(libs.app.cash.molecule.runtime)

    // Navigation3 + Serialization
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

**Remove** these if present (handled by BOM or not needed):
- `libs.androidx.lifecycle.runtime.ktx`
- `libs.androidx.compose.ui` / `libs.androidx.compose.ui.graphics` (covered by BOM)

**Add only if the feature set requires it** (not part of `init-app`):
- Ktor (`ktor-client-android`, etc.) — for HTTP networking
- SQLDelight (`android-driver`, `coroutines-extensions`) — for local DB
- Arrow Core (`arrow-core`) — for `Either` error handling

---

## Step 2 — Create the Application class

Create `<module>/src/main/java/<pkg>/App.kt`:

```kotlin
package <pkg>

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<KoinApplication> {
            androidContext(this@App)
        }
    }
}
```

The `@KoinApplication` annotation triggers the Koin compiler plugin to generate the module wiring. **No manual module registration is needed** — all `@Module @ComponentScan` classes on the classpath are discovered automatically.

---

## Step 3 — Register App in AndroidManifest.xml

Add `android:name=".App"` to the `<application>` element:

```xml
<application
    android:name=".App"
    android:allowBackup="true"
    ...>
```

---

## Step 4 — Create retainMolecule.kt

Create `<module>/src/main/java/<pkg>/retainMolecule.kt`. This bridges Molecule's `launchMolecule()` with `retain {}` so the presenter `StateFlow` survives configuration changes:

```kotlin
package <pkg>

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

@Composable
context(scope: CoroutineScope)
fun <T> retainMolecule(
    vararg keys: Any?,
    mode: RecompositionMode = RecompositionMode.ContextClock,
    context: CoroutineContext = scope.coroutineContext,
    body: @Composable () -> T
) = retain(*keys) {
    scope.launchMolecule(mode, context, body = body)
}
```

---

## Step 5 — Rewrite MainActivity.kt

Replace the entire file. The Activity sets up:
- A `CoroutineScope` tied to the Compose lifecycle via `retain`
- A `NavDisplay` with a `Route` sealed interface for type-safe navigation
- A single `Route.Home` stub entry — each new feature added by `create-feature` will add a route here

```kotlin
package <pkg>

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import <pkg>.ui.theme.<AppTheme>

@Serializable
sealed interface Route {
    data object Home : Route
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            <AppTheme> {
                context(retain { CoroutineScope(AndroidUiDispatcher.Main) }) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        val backStack = retain { mutableStateListOf<Route>(Route.Home) }
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
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
```

**Important**: Replace `<AppTheme>` with the theme composable that already exists in the module (e.g., `Arch26Theme`, `MyAppTheme`). Check `ui/theme/Theme.kt` for the exact name.

The `ui/theme/` package (`Color.kt`, `Theme.kt`, `Type.kt`) can be kept as-is or discarded — it has no effect on the architecture.

---

## Step 6 — Verify

```bash
./gradlew :<module>:assembleDebug
./gradlew :<module>:testDebugUnitTest
```

Both should pass before running `create-feature`.

---

## Checklist

- [ ] `build.gradle.kts` — added `koin.compiler` + `kotlin.serialization` plugins, Java 17, `-Xcontext-parameters`, `testOptions`, architecture dependencies
- [ ] `App.kt` — `@KoinApplication` Application class created
- [ ] `AndroidManifest.xml` — `android:name=".App"` added to `<application>`
- [ ] `retainMolecule.kt` — created with correct package
- [ ] `MainActivity.kt` — rewritten with `Route` sealed interface + `NavDisplay` scaffold
- [ ] `./gradlew :<module>:assembleDebug` passes
- [ ] `./gradlew :<module>:testDebugUnitTest` passes

Once all items are checked, the module is ready for **create-feature**.

---

## libs.versions.toml Aliases Reference

All aliases below must exist in `gradle/libs.versions.toml` before running this skill. They are already present in this project:

| Alias | Provides |
|---|---|
| `libs.plugins.koin.compiler` | `io.insert-koin.compiler.plugin` |
| `libs.plugins.jetbrains.kotlin.serialization` | `org.jetbrains.kotlin.plugin.serialization` |
| `libs.koin.bom` | `io.insert-koin:koin-bom` |
| `libs.koin.android` | `io.insert-koin:koin-android` |
| `libs.koin.annotations` | `io.insert-koin:koin-annotations` |
| `libs.koin.compose` | `io.insert-koin:koin-compose` |
| `libs.app.cash.molecule.runtime` | `app.cash.molecule:molecule-runtime` |
| `libs.androidx.navigation3.runtime` | `androidx.navigation3:navigation3-runtime` |
| `libs.androidx.navigation3.ui` | `androidx.navigation3:navigation3-ui` |
| `libs.kotlinx.serialization.core` | `org.jetbrains.kotlinx:kotlinx-serialization-core` |
| `libs.kotlinx.coroutines.test` | `org.jetbrains.kotlinx:kotlinx-coroutines-test` |
| `libs.turbine` | `app.cash.turbine:turbine` |

If any alias is missing from `libs.versions.toml`, add it before modifying `build.gradle.kts`.
