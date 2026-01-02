package com.example.todolist.ui.feature.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.FirebaseToDoRepositoryImpl
import com.example.todolist.domain.ToDo
import com.example.todolist.navigation.AddEditRoute
import com.example.todolist.ui.UiEvent
import com.example.todolist.ui.components.ToDoItem
import com.example.todolist.ui.feature.auth.AuthState
import com.example.todolist.ui.feature.auth.AuthViewModel
import com.example.todolist.ui.theme.TodoListTheme

@Composable
fun ListScreen(
    authViewModel: AuthViewModel,
    navigateToAddEditScreen: (id: String?) -> Unit,
    navigateToLoginScreen: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navigateToLoginScreen()
        }
    }

    val repository = remember {
        FirebaseToDoRepositoryImpl()
    }

    val viewModel = viewModel<ListViewModel> {
        ListViewModel(repository)
    }

    val todos by viewModel.todos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                is UiEvent.Navigate<*> -> {
                    when (val route = uiEvent.route) {
                        is AddEditRoute -> navigateToAddEditScreen(route.id)
                    }
                }
                is UiEvent.Signout -> authViewModel.signout()
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(uiEvent.message)
            }
        }
    }

    if (authState.value is AuthState.Authenticated) {
        ListContent(
            todos = todos,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { onEvent(ListEvent.Signout) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
                }

                Spacer(Modifier.weight(1f))

                FloatingActionButton(
                    onClick = { onEvent(ListEvent.AddEdit(null)) },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                }
            }
        }
    ) { padding ->
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma tarefa cadastrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
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
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
