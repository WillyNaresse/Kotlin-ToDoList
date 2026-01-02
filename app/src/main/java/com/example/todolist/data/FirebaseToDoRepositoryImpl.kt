package com.example.todolist.data

import com.example.todolist.domain.ToDo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseToDoRepositoryImpl : ToDoRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun tasksCollection() =
        firestore.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("tasks")

    override fun getAll(): Flow<List<ToDo>> = callbackFlow {
        val listener = tasksCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val todos = snapshot?.documents?.map { doc ->
                    ToDo(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description"),
                        isChecked = doc.getBoolean("isChecked") ?: false
                    )
                } ?: emptyList()

                trySend(todos).isSuccess
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun insert(
        title: String,
        description: String?,
        id: String?
    ) {
        val data = mapOf(
            "title" to title,
            "description" to description,
            "isChecked" to false
        )

        if (id == null) {
            tasksCollection().add(data).await()
        } else {
            tasksCollection().document(id).set(data).await()
        }
    }

    override suspend fun updateCompleted(id: String, isChecked: Boolean) {
        tasksCollection()
            .document(id)
            .update("isChecked", isChecked)
            .await()
    }

    override suspend fun delete(id: String) {
        tasksCollection()
            .document(id)
            .delete()
            .await()
    }

    override suspend fun getById(id: String): ToDo? {
        val doc = tasksCollection().document(id).get().await()
        return if (doc.exists()) {
            ToDo(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description"),
                isChecked = doc.getBoolean("isChecked") ?: false
            )
        } else {
            null
        }
    }
}