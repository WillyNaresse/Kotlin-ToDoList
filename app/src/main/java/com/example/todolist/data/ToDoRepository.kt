package com.example.todolist.data

import com.example.todolist.domain.ToDo
import kotlinx.coroutines.flow.Flow

interface ToDoRepository {

    suspend fun insert(
        title: String,
        description: String?,
        id: String? = null
    )

    suspend fun updateCompleted(id: String, isChecked: Boolean)

    suspend fun delete(id: String)

    fun getAll(): Flow<List<ToDo>>

    suspend fun getById(id: String): ToDo?
}
