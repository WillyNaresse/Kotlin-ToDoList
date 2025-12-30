package com.example.todolist.ui.feature.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.ui.UiEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email e/ou senha não podem estar em branco."
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
                _uiEvent.send(UiEvent.ShowSnackbar("Login realizado com sucesso!"))
            } catch (e: Exception) {
                _authState.value = AuthState.Error
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Ocorreu um erro no login."))
            }
        }
    }

    fun signup(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email e/ou senha não podem estar em branco."
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
                _uiEvent.send(UiEvent.ShowSnackbar("Cadastro realizado com sucesso! Bem-vindo(a)!"))
            } catch (e: Exception) {
                _authState.value = AuthState.Error
                _uiEvent.send(UiEvent.ShowSnackbar(e.message ?: "Ocorreu um erro no cadastro."))
            }
        }
    }

    fun signout() {
        viewModelScope.launch {

            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            _uiEvent.send(UiEvent.ShowSnackbar("Logout realizado."))
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}