package com.example.hiato.mvvm.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hiato.mvvm.viewmodel.GastosViewModel

@Composable
fun GastosView(
    grupoId: Int,
    onBack: () -> Unit,
    onOpenIntegrantes: (gastoId: Int, grupoId: Int, precio: Double) -> Unit,
    viewModel: GastosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddGastoDialog by remember { mutableStateOf(false) }
    var newGastoNombre by remember { mutableStateOf("") }
    var newGastoPrecio by remember { mutableStateOf("") }

    LaunchedEffect(grupoId) {
        viewModel.loadGastos(grupoId)
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
                onClick = { if (!uiState.isCreating) showAddGastoDialog = true },
                modifier = Modifier.align(Alignment.TopEnd),
                containerColor = if (uiState.isCreating) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = "Añadir Gasto",
                    tint = if (uiState.isCreating) {
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
                "Gastos (${uiState.gastos.size})",
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
                uiState.gastos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay gastos aún", fontSize = 18.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.gastos) { gasto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gasto.id?.let { onOpenIntegrantes(it, grupoId, gasto.precio) }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        gasto.nombre,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Precio: ${String.format("%.2f", gasto.precio)}€",
                                        fontSize = 16.sp,
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

    if (showAddGastoDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddGastoDialog = false
                newGastoNombre = ""
                newGastoPrecio = ""
                viewModel.clearError()
            },
            title = { Text("Añadir un Gasto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newGastoNombre,
                        onValueChange = { newGastoNombre = it },
                        label = { Text("Nombre del gasto") },
                        isError = uiState.error?.contains("Nombre") == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCreating,
                        supportingText = {
                            uiState.error?.takeIf { it.contains("Nombre") }?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = newGastoPrecio,
                        onValueChange = { newGastoPrecio = it },
                        label = { Text("Precio (€)") },
                        isError = uiState.error?.contains("Precio") == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCreating,
                        supportingText = {
                            uiState.error?.takeIf { it.contains("Precio") }?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val precio = newGastoPrecio.toDoubleOrNull()
                        viewModel.createGasto(
                            grupoId = grupoId,
                            nombre = newGastoNombre,
                            precio = precio ?: 0.0
                        ) {
                            showAddGastoDialog = false
                            newGastoNombre = ""
                            newGastoPrecio = ""
                        }
                    },
                    enabled = !uiState.isCreating
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Añadir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddGastoDialog = false
                        newGastoNombre = ""
                        newGastoPrecio = ""
                        viewModel.clearError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}