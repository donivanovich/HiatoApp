package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CuentaUiState(
    val currentUser: User? = null,
    val numGrupos: Int = 0,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null
)

class CuentaViewModel(
    private val repository: HiatoRepository = HiatoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuentaUiState(isLoading = true))
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    private var currentUserId: Int? = null

    fun loadCuenta(userId: Int) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val allUsers = repository.getUsers()
                val allGrupos = repository.getGrupos()

                val user = allUsers.find { it.id == userId }
                val numGrupos = allGrupos.count { it.userId == userId }

                _uiState.value = CuentaUiState(
                    currentUser = user,
                    numGrupos = numGrupos,
                    isLoading = false
                )

                println("CuentaViewModel userId=$userId: ${user?.nombre} tiene $numGrupos grupos")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando cuenta"
                )
            }
        }
    }

    fun updateUser(nombre: String, email: String, password: String, onSuccess: () -> Unit = {}) {
        val userId = currentUserId ?: return

        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Todos los campos son obligatorios"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

                val updatedUser = User(
                    id = userId,
                    nombre = nombre,
                    email = email,
                    password = password
                )

                val result = repository.updateUser(userId, updatedUser)

                // Recarga datos actualizados
                loadCuenta(userId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = "Error actualizando: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
