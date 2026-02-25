package com.example.hiato

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hiato.view.GruposScreen
import com.example.hiato.view.Login

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
            "main/{userId}",  // userId ahora es INT
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType  // ✅ Cambiado de StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1  // ✅ getInt()
            GruposScreen(navController, userId)  // Pasa Int directamente
        }
    }
}