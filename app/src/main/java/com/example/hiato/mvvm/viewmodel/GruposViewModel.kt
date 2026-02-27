package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Grupo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GruposUiState(
    val grupos: List<Grupo> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null
)

class GruposViewModel(
    private val repository: HiatoRepository = HiatoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GruposUiState(isLoading = true))
    val uiState: StateFlow<GruposUiState> = _uiState.asStateFlow()

    fun loadGrupos(userId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val allGrupos = repository.getGrupos()
                val misGrupos = allGrupos.filter { it.userId == userId }
                _uiState.value = GruposUiState(
                    grupos = misGrupos,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = GruposUiState(
                    isLoading = false,
                    error = e.message ?: "Error cargando grupos"
                )
            }
        }
    }

    fun createGrupo(userId: Int, nombre: String, onSuccess: () -> Unit = {}) {
        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre del grupo es requerido"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isCreating = true,
                    error = null
                )
                repository.addGrupo(userId, nombre)

                loadGrupos(userId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = GruposUiState(
                    isCreating = false,
                    error = "Error creando grupo: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
