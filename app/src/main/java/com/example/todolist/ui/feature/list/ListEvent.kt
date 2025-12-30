package com.example.todolist.ui.feature.list

sealed interface ListEvent {
    data class Delete(val id: Long) : ListEvent
    data class CompleteChanged(val id: Long, val isChecked: Boolean) : ListEvent
    data class AddEdit(val id: Long?) : ListEvent
    data class Signout(val id: Long?) : ListEvent
}