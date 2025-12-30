package com.example.todolist.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.ToDoRepository
import com.example.todolist.navigation.AddEditRoute
import com.example.todolist.ui.UiEvent
import com.example.todolist.ui.feature.auth.AuthViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListViewModel(
    private val repository: ToDoRepository,

) : ViewModel() {
    val todos = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: ListEvent) {
        when (event) {
            is ListEvent.Delete -> {
                delete(event.id)
            }

            is ListEvent.CompleteChanged -> {
                completeChanged(event.id, event.isChecked)
            }

            is ListEvent.AddEdit -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.Navigate(AddEditRoute(event.id)))
                }
            }

            is ListEvent.Signout -> {
                viewModelScope.launch {
                    _uiEvent.send(UiEvent.Signout)
                }
            }
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    private fun completeChanged(id: Long, isChecked: Boolean) {
        viewModelScope.launch {
            repository.updateCompleted(id, isChecked)
        }
    }
}