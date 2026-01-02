package com.example.todolist.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.todolist.ui.feature.addEdit.AddEditScreen
import com.example.todolist.ui.feature.auth.AuthState
import com.example.todolist.ui.feature.auth.AuthViewModel
import com.example.todolist.ui.feature.auth.login.LoginScreen
import com.example.todolist.ui.feature.auth.signup.SignUpScreen
import com.example.todolist.ui.feature.list.ListScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object SignUpRoute

@Serializable
object RoutesList

@Serializable
data class AddEditRoute(val id: String? = null)

@Composable
fun ToDoNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.observeAsState()

    if (authState == null || authState is AuthState.Loading) {
        LoadingScreen()
        return
    }

    val startDestination: Any = if (authState is AuthState.Authenticated) {
        RoutesList
    } else {
        LoginRoute
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginRoute> {
            LoginScreen(
                authViewModel = authViewModel,
                navigateToSignUp = { navController.navigate(SignUpRoute) },
                navigateToHome = {
                    navController.navigate(RoutesList) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<SignUpRoute> {
            SignUpScreen(
                authViewModel = authViewModel,
                navigateToLogin = { navController.navigate(LoginRoute) },
                navigateToHome = {
                    navController.navigate(RoutesList) {
                        popUpTo(SignUpRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<RoutesList> {
            ListScreen(
                authViewModel = authViewModel,
                navigateToLoginScreen = {
                    navController.navigate(LoginRoute) {
                        popUpTo(RoutesList) { inclusive = true }
                    }
                },
                navigateToAddEditScreen = { id ->
                    navController.navigate(AddEditRoute(id = id))
                }
            )
        }

        composable<AddEditRoute> {
            val addEditRoute = it.toRoute<AddEditRoute>()
            AddEditScreen(
                id = addEditRoute.id,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}