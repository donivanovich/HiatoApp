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

    // Estados para sub-navegación por tab (solo para Grupos por ahora)
    var grupoSeleccionado by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    if (grupoSeleccionado != null) {
                        GastosView(
                            grupoId = grupoSeleccionado!!,
                            userId = userId,
                            onBack = { grupoSeleccionado = null }
                        )
                    } else {
                        GruposView(
                            userId = userId,
                            onGrupoClick = { grupoId ->
                                grupoSeleccionado = grupoId
                            }
                        )
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
                // Reset sub-nav al cambiar tab (opcional)
                if (selectedTab != 0) grupoSeleccionado = null
            },
            userId = userId,
            navController = navController
        )
    }
}