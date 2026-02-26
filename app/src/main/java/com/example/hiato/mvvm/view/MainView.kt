package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.hiato.BottomNavigationBar

@Composable
fun MainView(
    navController: NavHostController,  // Para navegación global (login, etc.)
    userId: Int
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Estados para sub-navegación por tab
    var grupoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var gastoSeleccionado by remember { mutableStateOf<Int?>(null) }  // ← NUEVO

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    when {
                        gastoSeleccionado != null && grupoSeleccionado != null -> {
                            // Vista de Integrantes (nivel más profundo)
                            IntegrantesView(
                                gastoId = gastoSeleccionado!!,
                                grupoId = grupoSeleccionado!!,
                                onBack = {
                                    gastoSeleccionado = null  // Volver a Gastos
                                }
                            )
                        }
                        grupoSeleccionado != null -> {
                            // Vista de Gastos
                            GastosView(
                                grupoId = grupoSeleccionado!!,
                                userId = userId,
                                onBack = { grupoSeleccionado = null },  // Volver a Grupos
                                onOpenIntegrantes = { gastoId, grupoId ->
                                    gastoSeleccionado = gastoId
                                    // grupoId ya está guardado
                                }
                            )
                        }
                        else -> {
                            // Vista de Grupos (nivel inicial)
                            GruposView(
                                userId = userId,
                                onGrupoClick = { grupoId ->
                                    grupoSeleccionado = grupoId
                                }
                            )
                        }
                    }
                }
                1 -> AmigosView(navController, userId)
                2 -> CuentaView(navController, userId)
            }
        }

        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tabIndex ->
                selectedTab = tabIndex
                // Reset sub-nav al cambiar tab
                if (selectedTab != 0) {
                    grupoSeleccionado = null
                    gastoSeleccionado = null
                }
            },
            userId = userId,
            navController = navController
        )
    }
}
