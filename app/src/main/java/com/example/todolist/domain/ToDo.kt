package com.example.todolist.domain

data class ToDo(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val isChecked: Boolean = false
)