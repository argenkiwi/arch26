package com.gethomsefe.arch26.todo.data

import kotlinx.coroutines.flow.StateFlow

interface TodoRepository {
    val stateFlow: StateFlow<List<Todo>>
    suspend fun add(title: String)
    suspend fun toggle(id: String)
    suspend fun delete(id: String)
}
