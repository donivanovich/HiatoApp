package com.example.hiato.mvvm.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Gasto
import kotlinx.coroutines.launch

@Composable
fun GastosView(
    grupoId: Int,
    userId: Int,
    onBack: () -> Unit,
    onOpenIntegrantes: (gastoId: Int, grupoId: Int) -> Unit
) {
    println("GastosView grupoId=$grupoId, userId=$userId")
    var gastos by remember { mutableStateOf<List<Gasto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Estados para popup nuevo gasto
    var showAddGastoDialog by remember { mutableStateOf(false) }
    var newGastoNombre by remember { mutableStateOf("") }
    var newGastoPrecio by remember { mutableStateOf("") }

    LaunchedEffect(grupoId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allGastos = repo.getGastos()
                gastos = allGastos.filter { it.grupoId == grupoId }
                println("Gastos encontrados para grupo $grupoId: ${gastos.size}")
            } catch (e: Exception) {
                println("Error cargando gastos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header con DOS botones FAB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            // ← Botón Volver (izquierda)
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

            // + Botón Nuevo Gasto (derecha)
            FloatingActionButton(
                onClick = { showAddGastoDialog = true },
                modifier = Modifier.align(Alignment.TopEnd),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Gasto",
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
                "Gastos del Grupo $grupoId (${gastos.size})",
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
                gastos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay gastos aún", fontSize = 18.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(gastos) { gasto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        gasto.id?.let { onOpenIntegrantes(it, grupoId) }
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

    // Popup para añadir nuevo gasto
    if (showAddGastoDialog) {
        var nombreError by remember { mutableStateOf(false) }
        var precioError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showAddGastoDialog = false
                newGastoNombre = ""
                newGastoPrecio = ""
                nombreError = false
                precioError = false
            },
            title = { Text("Nuevo Gasto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newGastoNombre,
                        onValueChange = {
                            newGastoNombre = it
                            nombreError = false
                        },
                        label = { Text("Nombre del gasto") },
                        isError = nombreError,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (nombreError) Text("Nombre requerido")
                        }
                    )
                    OutlinedTextField(
                        value = newGastoPrecio,
                        onValueChange = {
                            newGastoPrecio = it
                            precioError = false
                        },
                        label = { Text("Precio (€)") },
                        isError = precioError,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (precioError) Text("Precio válido requerido")
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val precio = newGastoPrecio.toDoubleOrNull()
                        nombreError = newGastoNombre.isBlank()
                        precioError = precio == null || precio <= 0

                        if (!nombreError && !precioError) {
                            scope.launch {
                                try {
                                    val repo = HiatoRepository()
                                    repo.addGasto(grupoId, newGastoNombre, precio!!)

                                    // Recarga lista
                                    val allGastos = repo.getGastos()
                                    gastos = allGastos.filter { it.grupoId == grupoId }

                                    showAddGastoDialog = false
                                    newGastoNombre = ""
                                    newGastoPrecio = ""
                                } catch (e: Exception) {
                                    println("Error añadiendo gasto: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddGastoDialog = false
                        newGastoNombre = ""
                        newGastoPrecio = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
