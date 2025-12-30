package com.example.todolist.ui.feature.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.ToDoRepositoryImpl
import com.example.todolist.data.TodoDatabaseProvider
import com.example.todolist.domain.ToDo
import com.example.todolist.domain.todo1
import com.example.todolist.domain.todo2
import com.example.todolist.domain.todo3
import com.example.todolist.navigation.AddEditRoute
import com.example.todolist.ui.UiEvent
import com.example.todolist.ui.components.ToDoItem
import com.example.todolist.ui.feature.auth.AuthState
import com.example.todolist.ui.feature.auth.AuthViewModel
import com.example.todolist.ui.theme.TodoListTheme

@Composable
fun ListScreen(
    authViewModel: AuthViewModel,
    navigateToAddEditScreen: (id: Long?) -> Unit,
    navigateToLoginScreen: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navigateToLoginScreen()
        }
    }

    val context = LocalContext.current.applicationContext
    val database = TodoDatabaseProvider.provide(context)
    val repository = ToDoRepositoryImpl(database.todoDao)
    val viewModel = viewModel<ListViewModel> {
        ListViewModel(
            repository = repository
        )
    }

    val todos = viewModel.todos.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                is UiEvent.Navigate<*> -> {
                    when (uiEvent.route) {
                        is AddEditRoute -> {
                            navigateToAddEditScreen(uiEvent.route.id)
                        }
                    }
                }
                is UiEvent.Signout -> {
                    authViewModel.signout()
                }
            }
        }
    }

    if (authViewModel.authState.value is AuthState.Authenticated) {
        ListContent(
            todos = todos.value,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    todos: List<ToDo>,
    onEvent: (ListEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Minhas Tarefas",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { onEvent(ListEvent.Signout(null)) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 32.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                }

                Spacer(Modifier.weight(1f))

                FloatingActionButton(
                    onClick = { onEvent(ListEvent.AddEdit(null)) },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Tarefa")
                }
            }
        }
    ) { paddingValues ->
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma tarefa cadastrada no momento.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                itemsIndexed(todos) { index, todo ->
                    ToDoItem(
                        todo = todo,
                        onCheckedChange = {
                            onEvent(ListEvent.CompleteChanged(todo.id, it))
                        },
                        onItemClick = {
                            onEvent(ListEvent.AddEdit(todo.id))
                        },
                        onDeleteClick = {
                            onEvent(ListEvent.Delete(todo.id))
                        }
                    )

                    if (index < todos.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ListContentPreview() {
    TodoListTheme {
        ListContent(
            todos = listOf(
                todo1,
                todo2,
                todo3,
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun ListContentEmptyPreview() {
    TodoListTheme {
        ListContent(
            todos = emptyList(),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}