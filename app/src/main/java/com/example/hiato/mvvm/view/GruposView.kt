package com.example.hiato.mvvm.view

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Grupo
import kotlinx.coroutines.launch

@Composable
fun GruposView(
    userId: Int,
    onGrupoClick: (Int) -> Unit
) {
    println("GruposView userId = $userId")
    var grupos by remember { mutableStateOf<List<Grupo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // ✅ Estados para popup nuevo grupo
    var showAddGrupoDialog by remember { mutableStateOf(false) }
    var newGrupoNombre by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allGrupos = repo.getGrupos()
                grupos = allGrupos.filter { it.userId == userId }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ✅ FAB + Nuevo Grupo (arriba derecha)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { showAddGrupoDialog = true },
                modifier = Modifier.align(Alignment.TopEnd),
                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                            grupo.id?.let { grupoId ->
                                                onGrupoClick(grupoId)
                                            }
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
    }

    // ✅ Popup nuevo grupo
    if (showAddGrupoDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddGrupoDialog = false
                newGrupoNombre = ""
                nombreError = false
            },
            title = { Text("Nuevo Grupo") },
            text = {
                OutlinedTextField(
                    value = newGrupoNombre,
                    onValueChange = {
                        newGrupoNombre = it
                        nombreError = false
                    },
                    label = { Text("Nombre del grupo") },
                    isError = nombreError,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { if (nombreError) Text("Nombre requerido") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        nombreError = newGrupoNombre.isBlank()
                        if (!nombreError) {
                            scope.launch {
                                try {
                                    val repo = HiatoRepository()
                                    repo.addGrupo(userId, newGrupoNombre)

                                    // Recarga lista
                                    val allGrupos = repo.getGrupos()
                                    grupos = allGrupos.filter { it.userId == userId }

                                    showAddGrupoDialog = false
                                    newGrupoNombre = ""
                                } catch (e: Exception) {
                                    println("Error creando grupo: ${e.message}")
                                }
                            }
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddGrupoDialog = false
                        newGrupoNombre = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
