package com.example.hiato.mvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.GastoUser
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.launch

@Composable
fun IntegrantesView(
    gastoId: Int,
    grupoId: Int,
    onBack: () -> Unit
) {
    println("IntegrantesView gastoId=$gastoId, grupoId=$grupoId")

    var gastoUsers by remember { mutableStateOf<List<GastoUser>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Dialog para añadir integrante
    var showAddIntegranteDialog by remember { mutableStateOf(false) }
    var dialogUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUserId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(gastoId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val TODOS_GastoUsers = repo.getGastosUsers()
                println("🔍 TODOS gastoUsers: $TODOS_GastoUsers")
                println("🔍 Filtrando gastoId=$gastoId")

                gastoUsers = TODOS_GastoUsers.filter { it.gastoId == gastoId }
                println("🔍 DESPUÉS filtro: $gastoUsers")

                users = repo.getUsers()
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Carga users para dialog
    LaunchedEffect(showAddIntegranteDialog) {
        if (showAddIntegranteDialog) {
            val repo = HiatoRepository()
            dialogUsers = repo.getUsers()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header con DOS FABs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            // ← Volver (izquierda)
            FloatingActionButton(
                onClick = { onBack() },
                modifier = Modifier.align(Alignment.TopStart),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // + Añadir Integrante (derecha)
            FloatingActionButton(
                onClick = { showAddIntegranteDialog = true },
                modifier = Modifier.align(Alignment.TopEnd),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Integrante",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Integrantes del Gasto #$gastoId (${gastoUsers.size})",
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
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                isLoading = true
                                error = null
                            }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                gastoUsers.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No hay integrantes asignados", fontSize = 18.sp)
                            Text("Usa el + para añadir el primero", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(gastoUsers) { gastoUser ->
                            val user = users.find { it.id == gastoUser.userId }
                            user?.let {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // TODO: Detalles/editar asignación
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF3E5F5) // Púrpura claro exacto de tu imagen
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Sin sombra como foto
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp), // Padding interno exacto
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Avatar circular (gris púrpura como Ivan/Donnie)
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    color = Color(0xFF6200EE), // Morado avatar como foto
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (it.nombre?.firstOrNull()?.toString() ?: "?").uppercase(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Nombre + Email (vertical, debajo avatar)
                                        Column {
                                            Text(
                                                text = it.nombre ?: it.email ?: "Usuario",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = Color(0xFF1C1B1F) // Negro suave como foto
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = it.email ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF666666) // Gris como email foto
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

    // ✅ DIALOGO FUNCIONAL - Crea GastoUser real
    if (showAddIntegranteDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddIntegranteDialog = false
                selectedUserId = null
            },
            title = { Text("Asignar Integrante") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(1F)
                        .fillMaxWidth()
                ) {
                    items(dialogUsers) { user ->
                        if (user.id != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUserId = user.id
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedUserId == user.id,
                                    onClick = { selectedUserId = user.id }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.nombre ?: user.email ?: "Usuario ${user.id}")
                                    Text(
                                        "ID: ${user.id}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUserId?.let { userId ->
                            scope.launch {
                                try {
                                    val repo = HiatoRepository()
                                    val nuevoGastoUser = GastoUser(
                                        gastoId = gastoId,
                                        userId = userId
                                    )
                                    repo.addGastoUser(nuevoGastoUser)  // ✅ CREA EN BACKEND

                                    println("✅ CREADO: userId=$userId → gastoId=$gastoId")

                                    // Recarga lista
                                    gastoUsers = repo.getGastosUsers().filter { it.gastoId == gastoId }

                                    showAddIntegranteDialog = false
                                    selectedUserId = null
                                } catch (e: Exception) {
                                    println("❌ Error: ${e.message}")
                                    error = "Error asignando: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = selectedUserId != null
                ) {
                    Text("Asignar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddIntegranteDialog = false
                    selectedUserId = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
