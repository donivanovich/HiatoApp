package com.example.hiato

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hiato.mvvm.view.Login
import com.example.hiato.mvvm.view.MainView

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            Login(navController)
        }

        composable(
            "main/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            MainView(userId, navController = navController)
        }

        composable(
            "gastos/{grupoId}",
            arguments = listOf(
                navArgument("grupoId") { type = NavType.IntType },
                navArgument("userId") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getInt("grupoId") ?: 0
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
        }
    }
}
