package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.launch

@Composable
fun CuentaView(
    userId: Int
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var numGrupos by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Estados para edición
    var showEditDialog by remember { mutableStateOf(false) }
    var editNombre by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val repo = HiatoRepository()
                val allUsers = repo.getUsers()
                val allGrupos = repo.getGrupos()

                currentUser = allUsers.find { it.id == userId }
                numGrupos = allGrupos.count { it.userId == userId }

                println("CuentaView userId=$userId: ${currentUser?.nombre} tiene $numGrupos grupos")
            } catch (e: Exception) {
                println("Error cargando cuenta: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Función para actualizar usuario
    fun onUpdateUser() {
        if (currentUser == null || editNombre.isBlank() || editEmail.isBlank() || editPassword.isBlank()) {
            println("Usuario nulo o campos vacíos")
            return
        }
        scope.launch {
            try {
                isUpdating = true
                val repo = HiatoRepository()
                val updatedUser = User(
                    id = currentUser!!.id,
                    nombre = editNombre,
                    email = editEmail,
                    password = editPassword
                )
                val result = repo.updateUser(currentUser!!.id!!, updatedUser)
                currentUser = result
                println("Usuario actualizado: ${result.nombre}")
            } catch (e: Exception) {
                println("Error actualizando: ${e.message}")
            } finally {
                isUpdating = false
                showEditDialog = false
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
                        currentUser!!.nombre ?: "Sin nombre",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentUser!!.email ?: "Sin email",
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

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        editNombre = currentUser!!.nombre ?: ""
                        editEmail = currentUser!!.email ?: ""
                        editPassword = currentUser!!.password ?: ""
                        showEditDialog = true
                    },
                    enabled = currentUser != null,  // Deshabilita si no hay usuario
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Usuario")
                }
            }
        }
    }

    // Diálogo de edición
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Usuario") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()  // ← Oculta password
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onUpdateUser() },
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
