package com.example.hiato.mvvm.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hiato.BottomNavigationBar
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Grupo
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.launch

@Composable
fun MainView(
    navController: NavHostController,
    userId: Int
) {
    println("MainView userId = $userId")
    var selectedTab by remember { mutableStateOf(0) } // 0=Grupos, 1=Amigos, 2=Cuenta

    Column(modifier = Modifier.fillMaxSize()) {
        // Contenido según tab
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> GruposContent(navController, userId)
                1 -> AmigosContent(userId)
                2 -> CuentaContent(userId)
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tabIndex ->
                selectedTab = tabIndex  // Solo cambia tab local
            },
            userId = userId,
            navController = navController
        )
    }
}

@Composable
private fun GruposContent(navController: NavHostController, userId: Int) {
    var grupos by remember { mutableStateOf<List<Grupo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                grupos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes grupos aún", fontSize = 18.sp)
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(grupos) { grupo ->
                            Card(modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    println("🔥 CLICK GRUPO ID=${grupo.id} userId=$userId")  // ← AGREGAR
                                    navController.navigate("gastos/${grupo.id}?userId=$userId")
                                }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        grupo.nombre,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
}

@Composable
private fun AmigosContent(userId: Int) {
    var amigos by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allUsers = repo.getUsers()
                amigos = allUsers.filter { it.id != userId }
                println("amigos filtrados = ${amigos.size}")
            } catch (e: Exception) {
                println("Error amigos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                "Amigos (${amigos.size})",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                amigos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
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
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(end = 12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            user.nombre,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            user.email,
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
    }
}

@Composable
private fun CuentaContent(userId: Int) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var numGrupos by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allUsers = repo.getUsers()
                val allGrupos = repo.getGrupos()

                currentUser = allUsers.find { it.id == userId }
                numGrupos = allGrupos.count { it.userId == userId }
                println("Cuenta: $currentUser tiene $numGrupos grupos")
            } catch (e: Exception) {
                println("Error cuenta: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(64.dp))

            Surface(
                shape = CircleShape,
                tonalElevation = 8.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
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
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text("ID: $userId", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text("Total de grupos: $numGrupos", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
