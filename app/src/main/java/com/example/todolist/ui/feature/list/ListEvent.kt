package com.example.todolist.ui.feature.list

sealed class ListEvent {
    data class Delete(val id: String) : ListEvent()
    data class CompleteChanged(val id: String, val isChecked: Boolean) : ListEvent()
    data class AddEdit(val id: String?) : ListEvent()
    data object Signout : ListEvent()
}
