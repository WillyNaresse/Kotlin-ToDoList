package com.example.todolist.ui.feature.auth

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object Error : AuthState()
}