package com.example.hiato.mvvm.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hiato.mvvm.viewmodel.IntegrantesViewModel

@Composable
fun IntegrantesView(
    gastoId: Int,
    gastoPrecio: Double,
    onBack: () -> Unit,
    viewModel: IntegrantesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddIntegranteDialog by remember { mutableStateOf(false) }
    var newUserIdentifier by remember { mutableStateOf("") }

    LaunchedEffect(gastoId) {
        viewModel.loadIntegrantes(gastoId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
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
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Añadir Integrante",
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
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                "Integrantes (${uiState.integrantes.size})",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                uiState.error!!,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.clearError() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.integrantes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No hay integrantes aún", fontSize = 18.sp)
                        }
                    }
                }

                else -> {
                    val numIntegrantes = uiState.integrantes.size.coerceAtLeast(1)
                    val cantidadPorIntegrante = gastoPrecio / numIntegrantes

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.integrantes) { integrante ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO: Abrir detalle */ }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.small
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = integrante.user.nombre?.firstOrNull()?.toString()
                                                ?: "?",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            integrante.user.nombre ?: "Sin nombre",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            integrante.user.email ?: "",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = String.format("%.2f €", cantidadPorIntegrante),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddIntegranteDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddIntegranteDialog = false
                newUserIdentifier = ""
                viewModel.clearError()
            },
            title = { Text("Añadir un Integrante") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newUserIdentifier,
                        onValueChange = { newUserIdentifier = it },
                        label = { Text("Correo del usuario") },
                        isError = uiState.error != null || newUserIdentifier.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isAdding,
                        supportingText = {
                            if (uiState.error != null) {
                                Text(
                                    uiState.error!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    "Por ejemplo: user@example.com",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUserIdentifier.isNotBlank()) {
                            viewModel.addIntegrante(
                                gastoId = gastoId,
                                userEmail = newUserIdentifier
                            ) {
                                showAddIntegranteDialog = false
                                newUserIdentifier = ""
                            }
                        }
                    },
                    enabled = !uiState.isAdding && newUserIdentifier.isNotBlank()
                ) {
                    if (uiState.isAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Añadir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddIntegranteDialog = false
                        newUserIdentifier = ""
                        viewModel.clearError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
