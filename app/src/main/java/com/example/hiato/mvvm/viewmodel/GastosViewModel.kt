package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Gasto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GastosUiState(
    val gastos: List<Gasto> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null
)

class GastosViewModel(
    private val repository: HiatoRepository = HiatoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GastosUiState(isLoading = true))
    val uiState: StateFlow<GastosUiState> = _uiState.asStateFlow()

    fun loadGastos(grupoId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val allGastos = repository.getGastos()
                val gastosGrupo = allGastos.filter { it.grupoId == grupoId }
                _uiState.value = GastosUiState(
                    gastos = gastosGrupo,
                    isLoading = false
                )
                println("GastosViewModel: ${gastosGrupo.size} gastos para grupoId=$grupoId")
            } catch (e: Exception) {
                _uiState.value = GastosUiState(
                    isLoading = false,
                    error = e.message ?: "Error cargando gastos"
                )
            }
        }
    }

    fun createGasto(grupoId: Int, nombre: String, precio: Double, onSuccess: () -> Unit = {}) {
        if (nombre.isBlank() || precio <= 0) {
            _uiState.value = _uiState.value.copy(
                error = if (nombre.isBlank()) "Nombre requerido" else "Precio válido requerido"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreating = true, error = null)
                repository.addGasto(grupoId, nombre, precio)

                loadGastos(grupoId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = GastosUiState(
                    isCreating = false,
                    error = "Error creando gasto: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
