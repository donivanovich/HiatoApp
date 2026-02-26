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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hiato.mvvm.viewmodel.IntegrantesViewModel

@Composable
fun IntegrantesView(
    gastoId: Int,
    grupoId: Int,
    onBack: () -> Unit,
    viewModel: IntegrantesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddIntegranteDialog by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<Int?>(null) }

    // ✅ Recarga cuando cambia gastoId O termina isAdding
    LaunchedEffect(gastoId, uiState.isAdding) {
        println("🔧 LaunchedEffect: gastoId=$gastoId, isAdding=${uiState.isAdding}")
        viewModel.loadIntegrantes(gastoId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header con FABs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            FloatingActionButton(
                onClick = { if (!uiState.isAdding) showAddIntegranteDialog = true },
                modifier = Modifier.align(Alignment.TopEnd),
                containerColor = if (uiState.isAdding) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir integrante",
                    tint = if (uiState.isAdding) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
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
                text = "Integrantes del Gasto #$gastoId (${uiState.gastoUsers.size})",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Reintentar")
                        }
                    }
                }
                uiState.gastoUsers.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay integrantes asignados", fontSize = 18.sp)
                        Text(
                            "Usa el + para añadir",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // ✅ Key correcto en items()
                        items(
                            items = uiState.gastoUsers,
                            key = { gastoUser -> gastoUser.id ?: gastoUser.userId }
                        ) { gastoUser ->
                            val user = uiState.allUsers.find { it.id == gastoUser.userId }
                            user?.let {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { /* Detalle futuro */ }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF6200EE), CircleShape),
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = it.nombre ?: it.email ?: "Usuario",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = it.email ?: "",
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

    // Dialog añadir integrante
    if (showAddIntegranteDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddIntegranteDialog = false
                selectedUserId = null
                viewModel.clearError()
            },
            title = { Text("Asignar Integrante") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(uiState.allUsers) { user ->
                        user.id?.let { id ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUserId = id }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedUserId == id,
                                    onClick = { selectedUserId = id }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = user.nombre ?: user.email ?: "Usuario $id")
                                    Text(
                                        text = "ID: $id",
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
                            println("🔧 CLICK ASIGNAR: gastoId=$gastoId userId=$userId")
                            viewModel.addIntegrante(gastoId, userId) {
                                showAddIntegranteDialog = false
                                selectedUserId = null
                            }
                        }
                    },
                    enabled = selectedUserId != null && !uiState.isAdding
                ) {
                    if (uiState.isAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Asignar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddIntegranteDialog = false
                        selectedUserId = null
                        viewModel.clearError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
