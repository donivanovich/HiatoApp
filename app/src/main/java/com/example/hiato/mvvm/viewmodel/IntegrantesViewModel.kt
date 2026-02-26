package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.GastoUser
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class IntegrantesUiState(
    val gastoUsers: List<GastoUser> = emptyList(),
    val allUsers: List<User> = emptyList(),
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

    fun addIntegrante(gastoId: Int, userId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                println("🔧 addIntegrante INICIO: gastoId=$gastoId, userId=$userId")
                _uiState.value = _uiState.value.copy(isAdding = true, error = null)

                val newGastoUser = GastoUser(id = null, gastoId = gastoId, userId = userId)
                val created = repository.addGastoUser(newGastoUser)
                println("🔧 API RESPUESTA: $created")

                // ✅ DELAY + FORZAR refresh (MongoDB/Retrofit cache)
                delay(300)

                println("🔧 RECARGA FORZADA")
                currentGastoId?.let {
                    loadIntegrantes(it)
                }
                onSuccess()
            } catch (e: Exception) {
                println("🔧 ERROR: ${e.message}")
                _uiState.value = _uiState.value.copy(isAdding = false, error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isAdding = false)
            }
        }
    }

    suspend fun loadIntegrantes(gastoId: Int) {
        currentGastoId = gastoId
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            println("🔧 CARGANDO getGastosUsers...")
            val allGastoUsers = repository.getGastosUsers()
            println("🔧 API DEVUELVE ${allGastoUsers.size} gastoUsers total")

            val gastoUsers = allGastoUsers.filter { it.gastoId == gastoId }.toList()
            println("🔧 FILTRADOS ${gastoUsers.size} para gastoId=$gastoId")

            val allUsers = repository.getUsers().toList()

            // ✅ NUEVO estado COMPLETO
            _uiState.value = IntegrantesUiState(
                gastoUsers = gastoUsers,
                allUsers = allUsers,
                isLoading = false
            )
            println("🔧 UI STATE ACTUALIZADO: ${gastoUsers.size} items")

        } catch (e: Exception) {
            println("🔧 LOAD ERROR: ${e.message}")
            _uiState.value = IntegrantesUiState(
                isLoading = false,
                error = e.message
            )
        }
    }


    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
