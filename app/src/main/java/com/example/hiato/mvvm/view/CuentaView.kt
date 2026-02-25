package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hiato.BottomNavigationBar
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.launch

@Composable
fun CuentaView(
    navController: NavHostController,
    userId: Int
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var numGrupos by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(2) }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allUsers = repo.getUsers()
                val allGrupos = repo.getGrupos() // ← AÑADIDO: cargar grupos

                currentUser = allUsers.find { it.id == userId }
                // ← AÑADIDO: contar grupos del usuario
                numGrupos = allGrupos.count { it.userId == userId }

                println("CuentaView userId=$userId: ${currentUser?.nombre} tiene $numGrupos grupos")
            } catch (e: Exception) {
                println("Error cargando cuenta: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(64.dp))

                Surface(
                    shape = CircleShape,
                    tonalElevation = 8.dp,
                    modifier = Modifier.size(96.dp)
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        null,
                        modifier = Modifier.size(96.dp).clip(CircleShape),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else if (currentUser != null) {
                    Text(
                        currentUser!!.nombre,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentUser!!.email,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text("Usuario no encontrado", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.padding(end = 12.dp))
                            Text("ID: $userId", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, null, modifier = Modifier.padding(end = 12.dp))
                            Text("Total de grupos: $numGrupos", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        BottomNavigationBar(selectedTab, { selectedTab = it }, userId, navController)
    }
}
