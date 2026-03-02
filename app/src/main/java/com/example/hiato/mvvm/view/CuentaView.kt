package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hiato.mvvm.viewmodel.CuentaViewModel
import androidx.compose.foundation.layout.Arrangement
import com.example.hiato.LoginViewRoute

@Composable
fun CuentaView(
    userId: Int,
    navController: NavHostController,
    viewModel: CuentaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editNombre by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.loadCuenta(userId)
    }

    LaunchedEffect(uiState.currentUser) {
        uiState.currentUser?.let {
            editNombre = it.nombre ?: ""
            editEmail = it.email ?: ""
            editPassword = it.password ?: ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(64.dp))

                Surface(
                    shape = CircleShape,
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            uiState.isLoading || uiState.currentUser == null -> {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    null,
                                    modifier = Modifier.size(96.dp).clip(CircleShape),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            else -> {
                                val nombre = uiState.currentUser!!.nombre ?: ""
                                val initial = if (nombre.isNotEmpty()) {
                                    nombre.trim().uppercase().first().toString()
                                } else {
                                    "?"
                                }
                                Text(
                                    text = initial,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                    }
                    uiState.error != null -> {
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
                    uiState.currentUser != null -> {
                        Text(
                            uiState.currentUser!!.nombre ?: "Sin nombre",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            uiState.currentUser!!.email ?: "Sin email",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text("Usuario no encontrado", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                "ID: $userId",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Group,
                                null,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                "Total de grupos: ${uiState.numGrupos}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (uiState.currentUser != null) showEditDialog = true
                    },
                    enabled = uiState.currentUser != null && !uiState.isUpdating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Usuario")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showLogoutDialog = true },
                    enabled = uiState.currentUser != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Salir")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salir")
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                viewModel.clearError()
            },
            title = { Text("Editar Cuenta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre") },
                        isError = uiState.error?.contains("Nombre") == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isUpdating,
                        supportingText = {
                            uiState.error?.takeIf { it.contains("Nombre") }?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        isError = uiState.error?.contains("Email") == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isUpdating,
                        supportingText = {
                            uiState.error?.takeIf { it.contains("Email") }?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = uiState.error?.contains("Contraseña") == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isUpdating,
                        supportingText = {
                            uiState.error?.takeIf { it.contains("Contraseña") }?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUser(
                            nombre = editNombre,
                            email = editEmail,
                            password = editPassword
                        ) {
                            showEditDialog = false
                        }
                    },
                    enabled = !uiState.isUpdating && editNombre.isNotBlank() &&
                            editEmail.isNotBlank() && editPassword.isNotBlank()
                ) {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        viewModel.clearError()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirmar") },
            text = { Text("¿Estás seguro de cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        // SIMPLEMENTE VUELVE AL LOGIN sin limpiar nada
                        navController.navigate(LoginViewRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
