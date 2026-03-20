package com.gethomsefe.arch26.todo

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
class DatabaseTodoRepository(database: Database) : TodoRepository {
    private val queries = database.todoQueries
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override val todos: StateFlow<List<Todo>> = queries.getAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { Todo(id = it.id, title = it.title, completed = it.completed) } }
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
