package com.example.hiato.mvvm.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hiato.BottomNavigationBar
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Grupo
import kotlinx.coroutines.launch

@Composable
fun GruposScreen(
    navController: NavHostController,
    userId: Int
) {
    println("GruposScreen userId = $userId")
    var grupos by remember { mutableStateOf<List<Grupo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // Grupos activo

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allGrupos = repo.getGrupos()
                println("allGrupos = $allGrupos")
                allGrupos.forEach { println("Grupo: ${it.nombre}, userId=${it.userId}") }
                grupos = allGrupos.filter { it.userId == userId }
                println("grupos filtrados = $grupos")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Mis Grupos (${grupos.size})",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    grupos.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes grupos aún", fontSize = 18.sp)
                        }
                    }
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(grupos) { grupo ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Navega pasando grupoId y userId
                                            navController.navigate(
                                                "gastos/${grupo.id}?userId=$userId"
                                            )
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(grupo.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "ID Usuario: ${grupo.userId}",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tabIndex ->
                selectedTab = tabIndex
            },
            userId = userId,
            navController = navController
        )
    }
}
