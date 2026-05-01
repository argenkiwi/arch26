---
name: create-repository
description: Creates a Repository interface, its implementation, optional SQLDelight schema, and a Fake for testing — following the project's Koin + StateFlow architecture. Use this skill whenever the user wants to add a data layer for a feature, persist data, fetch from an API, connect a Presenter to real data, or asks about "repository", "data layer", "database table", or "API integration" in this project.
metadata:
  author: leandro
  version: "1.0"
---

# Create Repository

This skill guides the creation of the data layer for a feature: a repository interface, its implementation, optional SQLDelight schema, Koin wiring, and a `Fake` for unit tests.

---

## Step 1 — Decide the Backing Store

Before writing any code, identify what backs the repository:

| Scenario | Implementation type | File name pattern |
|---|---|---|
| Persisted data (todos, notes, etc.) | SQLDelight database | `Database{Feature}Repository` |
| Network data cached in memory | In-memory map + API | `InMemory{Feature}Repository` |
| Remote-only (no caching needed) | Simple suspend calls | `{Feature}Repository` impl inline |

Ask the user if not obvious.

---

## Step 2 — Define the Domain Model

Create a plain `data class` in `{feature}/data/`. Keep it free of network or database details — those belong in separate DTO / schema files.

```kotlin
// app/src/main/java/com/gethomsefe/arch26/<feature>/data/<Entity>.kt
package com.gethomsefe.arch26.<feature>.data

data class <Entity>(
    val id: String,
    val title: String,       // adjust fields to the domain
    val completed: Boolean
)
```

---

## Step 3 — Define the Repository Interface

Create `{Feature}Repository.kt` in the `{feature}/data/` package. The interface should be minimal and business-focused — no database or HTTP details leak through.

**Decide on the reactive shape:**
- Single flat `StateFlow<List<T>>` (no params) — for simple lists (e.g. todos)
- `fun stateFlow(param: X): StateFlow<List<T>>` — when data is keyed by a parameter (e.g. quakes by MMI)

```kotlin
// app/src/main/java/com/gethomsefe/arch26/<feature>/data/<Feature>Repository.kt
package com.gethomsefe.arch26.<feature>.data

import arrow.core.Either
import kotlinx.coroutines.flow.StateFlow

interface <Feature>Repository {
    // Reactive read — always expose at least one StateFlow for UI observation.
    val stateFlow: StateFlow<List<<Entity>>>            // flat variant
    // OR:
    fun stateFlow(key: SomeType): StateFlow<List<<Entity>>>  // keyed variant

    // Mutations — one suspend fun per business operation.
    suspend fun add(title: String)
    suspend fun toggle(id: String)
    suspend fun delete(id: String)
    // For network repos, fetch operations return Either<String, List<T>>:
    // suspend fun fetch(param: Int): Either<String, List<<Entity>>>
}
```

**Rules:**
- Always expose a `StateFlow` so the Presenter can `collectAsState()` reactively.
- Return `Either<String, T>` (from Arrow) for operations that can fail (typically network fetches).
- Return `Unit` for database mutations — they don't fail in observable ways.

---

## Step 4a — Database-backed Implementation (SQLDelight)

### 4a-i. Create the SQLDelight schema

Create a `.sq` file in `app/src/main/sqldelight/com/gethomesafe/arch26/`. The file name becomes the generated `Queries` class name.

```sql
-- app/src/main/sqldelight/com/gethomesafe/arch26/<Entity>.sq
import kotlin.Boolean;

CREATE TABLE <entity> (
  id TEXT NOT NULL PRIMARY KEY,
  title TEXT NOT NULL,
  completed INTEGER AS Boolean NOT NULL DEFAULT 0
);

getAll:
SELECT * FROM <entity>;

add:
INSERT INTO <entity> (id, title, completed) VALUES (?, ?, 0);

toggle:
UPDATE <entity> SET completed = 1 - completed WHERE id = ?;

delete:
DELETE FROM <entity> WHERE id = ?;
```

SQLDelight generates a `Database.<entity>Queries` accessor and typed query methods from this file. Rebuild (`./gradlew assembleDebug`) after adding the file so the generated code is available.

### 4a-ii. Check the Database provider

The project's `Database` object and its driver are already provided in `todo/data/Database.kt`. If this feature uses the **same** database file, no changes are needed — just add a new `.sq` file and the generated queries will be accessible as `database.<entity>Queries`.

If a **separate** database file is needed (rare), create a new provider following the same `@Factory driver` + `@Single database` pattern.

### 4a-iii. Write the implementation

```kotlin
// app/src/main/java/com/gethomsefe/arch26/<feature>/data/Database<Feature>Repository.kt
package com.gethomsefe.arch26.<feature>.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gethomesafe.arch26.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import java.util.UUID

@Singleton
class Database<Feature>Repository(database: Database) : <Feature>Repository {
    private val queries = database.<entity>Queries
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val stateFlow: StateFlow<List<<Entity>>> = queries.getAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { <Entity>(id = it.id, title = it.title, completed = it.completed) } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun add(title: String) {
        withContext(Dispatchers.IO) { queries.add(UUID.randomUUID().toString(), title) }
    }

    override suspend fun toggle(id: String) {
        withContext(Dispatchers.IO) { queries.toggle(id) }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) { queries.delete(id) }
    }
}
```

**Key points:**
- `@Singleton` — one instance shared across the app; Koin's `@ComponentScan` picks it up automatically.
- The repository owns its `CoroutineScope` (`SupervisorJob + Dispatchers.IO`) — this keeps the `StateFlow` alive as long as the process lives.
- `SharingStarted.Eagerly` ensures the flow starts collecting immediately, so UI never misses updates.
- All mutations run on `Dispatchers.IO` via `withContext` — never call database APIs on the main thread.

---

## Step 4b — In-memory / Network-backed Implementation

Use this when data comes from an API and is cached locally in a `MutableStateFlow` map.

```kotlin
// app/src/main/java/com/gethomsefe/arch26/<feature>/data/InMemory<Feature>Repository.kt
package com.gethomsefe.arch26.<feature>.data

import arrow.core.right
import com.gethomsefe.arch26.network.ErrorResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Singleton

@Singleton
class InMemory<Feature>Repository(private val api: <Feature>Api) : <Feature>Repository {
    private val cache = mutableMapOf<KeyType, MutableStateFlow<List<<Entity>>>>()
    private val emptyFlow by lazy { MutableStateFlow(emptyList<<Entity>>()) }

    override fun stateFlow(key: KeyType): StateFlow<List<<Entity>>> = cache[key] ?: emptyFlow

    override suspend fun fetch(key: KeyType) = api.get(key)
        .mapLeft { response ->
            when (response) {
                is ErrorResponse.Network   -> response.throwable.run { message ?: toString() }
                is ErrorResponse.Server    -> response.body
                is ErrorResponse.Unexpected -> response.throwable.run { message ?: toString() }
            }
        }
        .map { response ->
            val items = response.body.items.map { it.toDomain() }
            cache.getOrPut(key) { MutableStateFlow(emptyList()) }.value = items
            items
        }

    override suspend fun getOrFetch(key: KeyType) = when (val flow = cache[key]) {
        null -> fetch(key)
        else -> flow.value.right()
    }
}
```

For the API interface and Ktor implementation, follow the quakes pattern:
- Interface: `{Feature}Api.kt` with `suspend fun get(param): NetworkResponse<DTO, String>`
- Implementation: `Ktor{Feature}Api.kt` with `@Singleton` and a constructor-injected `HttpClient`
- Network models: `@Serializable` data classes mapping the JSON response
- Converter: `fun NetworkDTO.toDomain(): Entity` extension in `{Feature}Converter.kt`

---

## Step 5 — Koin Wiring

In most cases, **no extra wiring is needed**. The `@Singleton` annotation on the implementation is enough — the root `Arch26Module`'s `@ComponentScan` discovers it automatically across the entire `com.gethomsefe.arch26` package tree.

**When you do need a module file:**

Only create `{Feature}Module.kt` when the implementation depends on objects that can't carry Koin annotations themselves (e.g., `Database`, `HttpClient`). Use top-level annotated functions — no `@Module` class needed.

```kotlin
// app/src/main/java/com/gethomsefe/arch26/<feature>/data/<Feature>Module.kt
package com.gethomsefe.arch26.<feature>.data

import org.koin.core.annotation.Singleton

@Singleton
fun <feature>HttpClient(): HttpClient = HttpClient(Android) { /* configure */ }
```

The existing `Database` provider in `todo/data/Database.kt` is already registered and available to any `@Singleton` that declares it as a constructor parameter.

---

## Step 6 — Wire Into the Presenter

Inject the repository as a constructor parameter. The `@Factory` on the `Presenter` (or the standalone presenter function) lets Koin resolve it automatically.

**Class-based presenter (common pattern):**
```kotlin
@Factory
class Presenter(private val repository: <Feature>Repository) {
    @Composable
    operator fun invoke(): State {
        val items by repository.stateFlow.collectAsState()
        val scope = rememberCoroutineScope()
        return State(
            items = items,
            perform = { action ->
                scope.launch {
                    when (action) {
                        is Action.Add    -> repository.add(action.title)
                        is Action.Delete -> repository.delete(action.id)
                    }
                }
            }
        )
    }
}
```

**Function-based presenter (simpler features):**
```kotlin
@Factory
fun presenter(repository: <Feature>Repository) = <Feature>Model.Presenter {
    val items by repository.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    <Feature>Model.State(
        items = items,
        perform = { action ->
            scope.launch {
                when (action) { /* ... */ }
            }
        }
    )
}
```

**For network repositories** — use `rememberWorker` or `rememberLoader` to drive the fetch and expose `Worker` state to the UI:
```kotlin
var loader by rememberWorker(Busy(false)) { refresh: Boolean ->
    with(repository) { if (refresh) fetch(param) else getOrFetch(param) }
}
val items by repository.stateFlow(param).collectAsState()
```

---

## Step 7 — Create a Fake for Tests

The Presenter test will need a fake implementation. Place it as a private inner class inside the test file.

```kotlin
private class Fake<Feature>Repository : <Feature>Repository {
    private val _stateFlow = MutableStateFlow<List<<Entity>>>(emptyList())
    override val stateFlow: StateFlow<List<<Entity>>> = _stateFlow

    override suspend fun add(title: String) {
        _stateFlow.value += <Entity>(id = UUID.randomUUID().toString(), title = title, completed = false)
    }

    override suspend fun toggle(id: String) {
        _stateFlow.value = _stateFlow.value.map { if (it.id == id) it.copy(completed = !it.completed) else it }
    }

    override suspend fun delete(id: String) {
        _stateFlow.value = _stateFlow.value.filter { it.id != id }
    }
}
```

The fake drives state changes directly so Turbine can assert on them without any real database or network.

---

## File Checklist

| File | Notes |
|---|---|
| `{feature}/data/{Entity}.kt` | Domain data class |
| `{feature}/data/{Feature}Repository.kt` | Interface |
| `{feature}/data/Database{Feature}Repository.kt` | SQLDelight impl (or `InMemory…`) |
| `sqldelight/…/{Entity}.sq` | DB schema + named queries (if SQLDelight) |
| `{feature}/data/{Feature}Module.kt` | Only if third-party deps needed |
| `{feature}/{Feature}Model.kt` | Inject repository into `@Factory Presenter` |
| `{feature}/{Feature}ModelPresenterTest.kt` | `Fake{Feature}Repository` + Turbine tests |

After adding a `.sq` file always rebuild:
```bash
./gradlew assembleDebug
```

Run presenter tests:
```bash
./gradlew test --tests "*.<Feature>ModelPresenterTest"
```

---

## Complete Example — "Notes" Feature (SQLDelight)

**Files:**
```
app/src/main/java/com/gethomsefe/arch26/notes/data/
    Note.kt
    NotesRepository.kt
    DatabaseNotesRepository.kt
app/src/main/sqldelight/com/gethomesafe/arch26/
    Note.sq
app/src/main/java/com/gethomsefe/arch26/notes/
    NotesModel.kt   ← injects NotesRepository
app/src/test/java/com/gethomsefe/arch26/notes/
    NotesModelPresenterTest.kt  ← uses FakeNotesRepository
```

**`Note.sq`:**
```sql
CREATE TABLE note (
  id   TEXT NOT NULL PRIMARY KEY,
  body TEXT NOT NULL
);

getAll:
SELECT * FROM note;

add:
INSERT INTO note (id, body) VALUES (?, ?);

delete:
DELETE FROM note WHERE id = ?;
```

**`Note.kt`:**
```kotlin
data class Note(val id: String, val body: String)
```

**`NotesRepository.kt`:**
```kotlin
interface NotesRepository {
    val stateFlow: StateFlow<List<Note>>
    suspend fun add(body: String)
    suspend fun delete(id: String)
}
```

**`DatabaseNotesRepository.kt`:**
```kotlin
@Singleton
class DatabaseNotesRepository(database: Database) : NotesRepository {
    private val queries = database.noteQueries
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override val stateFlow = queries.getAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { Note(it.id, it.body) } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    override suspend fun add(body: String) =
        withContext(Dispatchers.IO) { queries.add(UUID.randomUUID().toString(), body) }
    override suspend fun delete(id: String) =
        withContext(Dispatchers.IO) { queries.delete(id) }
}
```

**`NotesModel.kt` (Presenter excerpt):**
```kotlin
@Factory
class Presenter(private val repository: NotesRepository) {
    @Composable
    operator fun invoke(): State {
        val notes by repository.stateFlow.collectAsState()
        val scope = rememberCoroutineScope()
        return State(notes = notes, perform = { action ->
            scope.launch {
                when (action) {
                    is Action.Add    -> repository.add(action.body)
                    is Action.Delete -> repository.delete(action.id)
                }
            }
        })
    }
}
```
