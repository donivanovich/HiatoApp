package com.example.hiato.mvvm.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hiato.mvvm.viewmodel.GruposViewModel

@Composable
fun GruposView(
    userId: Int,
    onGrupoClick: (Int) -> Unit,
    viewModel: GruposViewModel = viewModel()  // ✅ ViewModel inyectado
) {
    println("GruposView userId = $userId")
    val uiState by viewModel.uiState.collectAsState()  // ✅ Estado reactivo único

    // ✅ Solo estados UI del dialog (mínimos)
    var showAddGrupoDialog by remember { mutableStateOf(false) }
    var newGrupoNombre by remember { mutableStateOf("") }

    // ✅ Carga automática al entrar/cambiar userId
    LaunchedEffect(userId) {
        viewModel.loadGrupos(userId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ✅ FAB Nuevo Grupo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            FloatingActionButton(
                onClick = {
                    if (!uiState.isCreating) showAddGrupoDialog = true
                },
                modifier = Modifier.align(Alignment.TopEnd),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                interactionSource = if (uiState.isCreating) {
                    // ✅ Deshabilitado: no responde a clicks ni ripple
                    remember { MutableInteractionSource() }
                } else {
                    interactionSource
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Grupo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Mis Grupos (${uiState.grupos.size})",  // ✅ Del ViewModel
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
                    uiState.error != null -> {  // ✅ Manejo de errores del VM
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
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
                    uiState.grupos.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes grupos aún", fontSize = 18.sp)
                        }
                    }
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.grupos) { grupo ->  // ✅ Lista del ViewModel
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            grupo.id?.let { onGrupoClick(it) }
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

    // ✅ Dialog simplificado - lógica en ViewModel
    if (showAddGrupoDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddGrupoDialog = false
                newGrupoNombre = ""
                viewModel.clearError()
            },
            title = { Text("Nuevo Grupo") },
            text = {
                OutlinedTextField(
                    value = newGrupoNombre,
                    onValueChange = { newGrupoNombre = it },
                    label = { Text("Nombre del grupo") },
                    isError = uiState.error?.contains("requerido") == true,  // ✅ Error del VM
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isCreating,  // ✅ Deshabilitado mientras crea
                    supportingText = {
                        uiState.error?.takeIf { it.contains("requerido") }?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createGrupo(
                            userId = userId,
                            nombre = newGrupoNombre
                        ) {
                            // ✅ Se cierra automáticamente al éxito (VM recarga lista)
                            showAddGrupoDialog = false
                            newGrupoNombre = ""
                        }
                    },
                    enabled = !uiState.isCreating
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Crear")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddGrupoDialog = false
                        newGrupoNombre = ""
                        viewModel.clearError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
