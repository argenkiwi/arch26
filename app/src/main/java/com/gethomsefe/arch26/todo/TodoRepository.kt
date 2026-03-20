package com.gethomsefe.arch26.todo

import kotlinx.coroutines.flow.StateFlow

interface TodoRepository {
    val todos: StateFlow<List<Todo>>
    suspend fun add(title: String)
    suspend fun toggle(id: String)
    suspend fun delete(id: String)
}
