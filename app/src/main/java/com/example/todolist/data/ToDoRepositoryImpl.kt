package com.example.todolist.data

import com.example.todolist.domain.ToDo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ToDoRepositoryImpl(
    private val dao: ToDoDao
): ToDoRepository {
    override suspend fun insert(title: String, description: String?, id: Long?) {
        val entity = id?.let {
            dao.getById(it)?.copy(
                title = title,
                description = description
            )
        } ?: ToDoEntity(
            title = title,
            description = description,
            isChecked = false
        )

        dao.insert(entity)
    }

    override suspend fun updateCompleted(id: Long, isChecked: Boolean) {
        val existingEntity = dao.getById(id) ?: return
        val updatedEntity = existingEntity.copy(isChecked = isChecked)
        dao.insert(updatedEntity)

    }

    override suspend fun delete(id: Long) {
        val existingEntity = dao.getById(id) ?: return
        dao.delete(existingEntity)
    }

    override fun getAll(): Flow<List<ToDo>> {
        return dao.getAll().map { entities ->
            entities.map { entity ->
                ToDo(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    isChecked = entity.isChecked
                )
            }
        }
    }

    override suspend fun getById(id: Long): ToDo? {
        return dao.getById(id)?.let { entity ->
            ToDo(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                isChecked = entity.isChecked
            )
        }
    }
}