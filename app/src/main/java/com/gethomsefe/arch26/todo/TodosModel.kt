package com.gethomsefe.arch26.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.gethomsefe.arch26.todo.data.Todo
import com.gethomsefe.arch26.todo.data.TodoRepository
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

    fun interface Presenter {

        @Composable
        operator fun invoke(): State
    }
}

@Factory
fun presenter(repository: TodoRepository) = TodosModel.Presenter {
    val todos = repository.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    TodosModel.State(
        todos = todos.value,
        perform = { action ->
            scope.launch {
                when (action) {
                    is TodosModel.Action.Add -> repository.add(action.title)
                    is TodosModel.Action.Toggle -> repository.toggle(action.id)
                    is TodosModel.Action.Delete -> repository.delete(action.id)
                }
            }
        }
    )
}
