package com.example.hiato.mvvm.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.hiato.data.HiatoRepository
import kotlinx.coroutines.launch

@Composable
fun Login(
    navController: NavHostController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSignupDialog by remember { mutableStateOf(false) }
    var signupEmail by remember { mutableStateOf("") }
    var signupNombre by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupIsLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar Sesión", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val repo = HiatoRepository()
                            val allUsers = repo.getUsers()
                            val user = allUsers.find {
                                it.email == email.trim() && it.password == password
                            }

                            if (user != null) {
                                navController.navigate("main/${user.id}") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                snackbarHostState.showSnackbar("Email o contraseña incorrectos")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error conexión: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    Text("Cargando...")
                } else {
                    Text("Entrar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { showSignupDialog = true },
                enabled = !isLoading
            ) {
                Text("¿Eres nuevo? Crea tu cuenta")
            }
        }
    }

    if (showSignupDialog) {
        AlertDialog(
            onDismissRequest = {
                showSignupDialog = false
                signupEmail = ""
                signupNombre = ""
                signupPassword = ""
            },
            title = { Text("Crear Cuenta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = signupEmail,
                        onValueChange = { signupEmail = it },
                        label = { Text("Email") },
                        isError = !signupEmail.contains("@"),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !signupIsLoading,
                        supportingText = {
                            if (!signupEmail.contains("@") || !signupEmail.contains(".") ||
                                !signupEmail.substringAfterLast("@").contains(".")) {
                                Text("Email inválido", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = signupNombre,
                        onValueChange = { signupNombre = it },
                        label = { Text("Nombre") },
                        isError = signupNombre.trim().isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !signupIsLoading,
                        supportingText = {
                            if (signupNombre.trim().isBlank()) {
                                Text("Nombre incompleto", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = signupPassword,
                        onValueChange = { signupPassword = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = signupPassword.length < 4,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !signupIsLoading,
                        supportingText = {
                            if (signupPassword.length < 4) {
                                Text("Mínimo 4 caracteres", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            signupIsLoading = true
                            try {
                                val repo = HiatoRepository()
                                val newUser = repo.createUser(
                                    signupEmail.trim().lowercase(),
                                    signupNombre.trim(),
                                    signupPassword
                                )
                                snackbarHostState.showSnackbar("Usuario creado correctamente")
                                showSignupDialog = false
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            } finally {
                                signupIsLoading = false
                            }
                        }
                    },
                    enabled = !signupIsLoading &&
                            signupEmail.contains("@") &&
                            signupNombre.trim().isNotBlank() &&
                            signupPassword.length >= 4,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (signupIsLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Crear")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSignupDialog = false
                        signupEmail = ""
                        signupNombre = ""
                        signupPassword = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}