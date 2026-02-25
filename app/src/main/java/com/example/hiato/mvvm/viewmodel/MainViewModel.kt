package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Gasto
import com.example.hiato.mvvm.model.Grupo
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val users: List<User> = emptyList(),
    val grupos: List<Grupo> = emptyList(),
    val gastos: List<Gasto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    // Idealmente inyectado con Hilt/Koin
    private val repo: HiatoRepository
) : ViewModel() {

    // StateFlow privado mutable + público inmutable
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            // Estado de carga
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val users = repo.getUsers()
                val grupos = repo.getGrupos()
                val gastos = repo.getGastos()

                _uiState.update {
                    it.copy(
                        users = users,
                        grupos = grupos,
                        gastos = gastos,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (ce: CancellationException) {
                // No silenciar cancelaciones
                throw ce
            } catch (e: Exception) {
                // Manejo de errores: mostrar mensaje en UI
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar datos"
                    )
                }
            }
        }
    }
}
