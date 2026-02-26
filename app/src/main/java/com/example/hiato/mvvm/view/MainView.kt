package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.hiato.BottomNavigationBar
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.viewmodel.AmigosViewModel
import com.example.hiato.mvvm.viewmodel.CuentaViewModel
import com.example.hiato.mvvm.viewmodel.GastosViewModel
import com.example.hiato.mvvm.viewmodel.GruposViewModel
import com.example.hiato.mvvm.viewmodel.IntegrantesViewModel

@Composable
fun MainView(
    navController: NavHostController,
    userId: Int
) {
    var selectedTab by remember { mutableStateOf(0) }
    var grupoSeleccionado by remember { mutableStateOf<Int?>(null) }
    var gastoSeleccionado by remember { mutableStateOf<Int?>(null) }
    val repo = HiatoRepository()
    val amigosViewModel = remember(repo) { AmigosViewModel(repo) }
    val gruposViewModel = remember(repo) { GruposViewModel(repo) }
    val gastosViewModel = remember(repo) { GastosViewModel(repo) }
    val integrantesViewModel = remember(repo) { IntegrantesViewModel(repo) }
    val cuentaViewModel = remember(repo) { CuentaViewModel(repo) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    when {
                        gastoSeleccionado != null && grupoSeleccionado != null -> {
                            IntegrantesView(
                                gastoId = gastoSeleccionado!!,
                                onBack = { gastoSeleccionado = null },
                                viewModel = integrantesViewModel
                            )
                        }
                        grupoSeleccionado != null -> {
                            GastosView(
                                grupoId = grupoSeleccionado!!,
                                onBack = { grupoSeleccionado = null },
                                onOpenIntegrantes = { gastoId, grupoId ->
                                    gastoSeleccionado = gastoId
                                },
                                viewModel = gastosViewModel
                            )
                        }
                        else -> {
                            GruposView(
                                userId = userId,
                                onGrupoClick = { grupoId ->
                                    grupoSeleccionado = grupoId
                                },
                                viewModel = gruposViewModel
                            )
                        }
                    }
                }
                1 -> AmigosView(userId, amigosViewModel)
                2 -> CuentaView(userId, viewModel = cuentaViewModel)
            }
        }

        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tabIndex ->
                selectedTab = tabIndex
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
