package com.example.hiato

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.hiato.mvvm.view.Login
import kotlinx.serialization.Serializable
import com.example.hiato.mvvm.view.MainView

@Serializable
object LoginViewRoute

@Serializable
data class MainViewRoute(val userId: Int)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginViewRoute
    ) {
        composable<LoginViewRoute> {
            Login(navController)
        }

        composable<MainViewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MainViewRoute>()
            MainView(userId = route.userId, navController = navController)
        }
    }
}
