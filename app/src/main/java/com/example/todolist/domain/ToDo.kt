package com.example.todolist.domain

class ToDo (
    val id: Long,
    val title: String,
    val description: String?,
    val isChecked: Boolean
)

// mocks
val todo1 = ToDo(1, "Title 1", "Description 1", false);
val todo2 = ToDo(2, "Title 2", "Description 2", true);
val todo3 = ToDo(3, "Title 3", "Description 3", false);