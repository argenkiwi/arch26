package com.gethomsefe.arch26.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

object TodosModel {
    sealed interface Action {
        data class Add(val title: String) : Action
        data class Toggle(val id: String) : Action
        data class Delete(val id: String) : Action
    }

    data class State(
        val todos: List<Todo>,
        val perform: (Action) -> Unit
    )

    @Factory
    class Presenter(private val repository: TodoRepository) {

        @Composable
        operator fun invoke(): State {
            val todos = repository.todos.collectAsState()
            val scope = rememberCoroutineScope()
            return State(
                todos = todos.value,
                perform = { action ->
                    scope.launch {
                        when (action) {
                            is Action.Add -> repository.add(action.title)
                            is Action.Toggle -> repository.toggle(action.id)
                            is Action.Delete -> repository.delete(action.id)
                        }
                    }
                }
            )
        }
    }
}
