package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hiato.BottomNavigationBar
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.launch

@Composable
fun AmigosView(
    navController: NavHostController,
    userId: Int
) {
    var amigos by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(1) } // Amigos activo

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allUsers = repo.getUsers()
                amigos = allUsers.filter { it.id != userId }
            } catch (e: Exception) {
                println("Error cargando amigos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp)) {
            Column {
                Text(
                    "Amigos (${amigos.size})",
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
                    amigos.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No tienes amigos aún.\n¡Crea grupos para invitarlos!",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(amigos) { user ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            null,
                                            modifier = Modifier.size(48.dp).padding(end = 12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(user.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            Text(user.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomNavigationBar(selectedTab, { selectedTab = it }, userId, navController)
    }
}