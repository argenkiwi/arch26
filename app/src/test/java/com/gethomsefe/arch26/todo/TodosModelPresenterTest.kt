package com.gethomsefe.arch26.todo

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.gethomsefe.arch26.todo.data.Todo
import com.gethomsefe.arch26.todo.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class TodosModelPresenterTest {

    private class FakeTodoRepository : TodoRepository {
        private val _stateFlow = MutableStateFlow<List<Todo>>(emptyList())
        override val stateFlow: StateFlow<List<Todo>> = _stateFlow

        override suspend fun add(title: String) {
            _stateFlow.value += Todo(id = UUID.randomUUID().toString(), title = title, completed = false)
        }

        override suspend fun toggle(id: String) {
            _stateFlow.value = _stateFlow.value.map { todo ->
                if (todo.id == id) todo.copy(completed = !todo.completed) else todo
            }
        }

        override suspend fun delete(id: String) {
            _stateFlow.value = _stateFlow.value.filter { it.id != id }
        }
    }

    @Test
    fun `initial state has empty todos list`() = runTest {
        val presenter = presenter(FakeTodoRepository())
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                assertTrue(todos.isEmpty())
            }
        }
    }

    @Test
    fun `Add action adds a todo`() = runTest {
        val presenter = presenter(FakeTodoRepository())
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                assertTrue(todos.isEmpty())
                perform(TodosModel.Action.Add("Buy milk"))
            }
            with(awaitItem()) {
                assertEquals(1, todos.size)
                assertEquals("Buy milk", todos.first().title)
                assertFalse(todos.first().completed)
            }
        }
    }

    @Test
    fun `Toggle action flips completed state`() = runTest {
        val repository = FakeTodoRepository()
        repository.add("Buy milk")
        val presenter = presenter(repository)
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                val todo = todos.first()
                assertFalse(todo.completed)
                perform(TodosModel.Action.Toggle(todo.id))
            }
            with(awaitItem()) {
                assertTrue(todos.first().completed)
                perform(TodosModel.Action.Toggle(todos.first().id))
            }
            with(awaitItem()) {
                assertFalse(todos.first().completed)
            }
        }
    }

    @Test
    fun `Delete action removes the todo`() = runTest {
        val repository = FakeTodoRepository()
        repository.add("Buy milk")
        val presenter = presenter(repository)
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                assertEquals(1, todos.size)
                perform(TodosModel.Action.Delete(todos.first().id))
            }
            with(awaitItem()) {
                assertTrue(todos.isEmpty())
            }
        }
    }

    @Test
    fun `Add multiple todos preserves all items`() = runTest {
        val presenter = presenter(FakeTodoRepository())
        moleculeFlow(mode = RecompositionMode.Immediate) {
            presenter.invoke()
        }.test {
            with(awaitItem()) {
                perform(TodosModel.Action.Add("First"))
            }
            with(awaitItem()) {
                perform(TodosModel.Action.Add("Second"))
            }
            with(awaitItem()) {
                assertEquals(2, todos.size)
                assertEquals("First", todos[0].title)
                assertEquals("Second", todos[1].title)
            }
        }
    }
}
