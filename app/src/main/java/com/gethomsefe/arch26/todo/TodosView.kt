package com.gethomsefe.arch26.todo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gethomsefe.arch26.retainMolecule
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

object TodosView {

    @Composable
    context(scope: CoroutineScope)
    fun Pane(modifier: Modifier = Modifier) {
        val presenter = koinInject<TodosModel.Presenter>()
        val stateFlow = retainMolecule { presenter.invoke() }
        val state by stateFlow.collectAsStateWithLifecycle()
        Pane(modifier, state)
    }

    @Composable
    fun Pane(modifier: Modifier, state: TodosModel.State) {
        Column(modifier = modifier) {
            AddTodoRow(onAdd = { state.perform(TodosModel.Action.Add(it)) })
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.todos, key = { it.id }) { todo ->
                    ListItem(
                        leadingContent = {
                            Checkbox(
                                checked = todo.completed,
                                onCheckedChange = { state.perform(TodosModel.Action.Toggle(todo.id)) }
                            )
                        },
                        headlineContent = {
                            Text(
                                text = todo.title,
                                textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                            )
                        },
                        trailingContent = {
                            TextButton(onClick = { state.perform(TodosModel.Action.Delete(todo.id)) }) {
                                Text("Delete")
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun AddTodoRow(onAdd: (String) -> Unit) {
        var text by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("New todo...") },
                singleLine = true
            )
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onAdd(text.trim())
                        text = ""
                    }
                }
            ) {
                Text("Add")
            }
        }
    }
}
