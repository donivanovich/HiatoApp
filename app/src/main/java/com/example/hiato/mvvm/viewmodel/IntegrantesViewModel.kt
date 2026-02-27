package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Integrante(
    val user: User,
    val gastoUserId: Int? = null
)

data class IntegrantesUiState(
    val integrantes: List<Integrante> = emptyList(),
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val error: String? = null
)

class IntegrantesViewModel(
    private val repository: HiatoRepository = HiatoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntegrantesUiState(isLoading = true))
    val uiState: StateFlow<IntegrantesUiState> = _uiState.asStateFlow()

    private var currentGastoId: Int? = null

    fun loadIntegrantes(gastoId: Int) {
        currentGastoId = gastoId
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val gastosUsers = repository.getGastosUsers()
                val todosUsers = repository.getUsers()

                val gastosUsersDelGasto = gastosUsers.filter { it.gastoId == gastoId }
                val integrantes = gastosUsersDelGasto.mapNotNull { gastoUser ->
                    todosUsers.find { it.id == gastoUser.userId }?.let { user ->
                        Integrante(user = user, gastoUserId = gastoUser.id)
                    }
                }

                _uiState.value = IntegrantesUiState(
                    integrantes = integrantes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = IntegrantesUiState(
                    isLoading = false,
                    error = e.message ?: "Error cargando integrantes"
                )
            }
        }
    }

    fun addIntegrante(gastoId: Int, userEmail: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAdding = true, error = null)

                val todosUsers = repository.getUsers()
                val user = todosUsers.find { it.email == userEmail }

                val userId = user?.id ?: run {
                    _uiState.value = _uiState.value.copy(
                        isAdding = false,
                        error = "Usuario con correo '$userEmail' no encontrado"
                    )
                    return@launch
                }

                repository.addGastoUser(gastoId = gastoId, userId = userId)

                loadIntegrantes(gastoId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAdding = false,
                    error = "Error añadiendo integrante: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
