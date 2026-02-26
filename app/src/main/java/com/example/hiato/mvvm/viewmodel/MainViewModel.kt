package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.Gasto
import com.example.hiato.mvvm.model.GastoUser
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
    val gastosUsers: List<GastoUser> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(private val repo: HiatoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init { loadAllData() }

    fun loadUsers() = viewModelScope.launch {
        try {
            val users = repo.getUsers()
            _uiState.update { it.copy(users = users, errorMessage = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.message) }
        }
    }

    fun loadIntegrantesForGasto(gastoId: Int) = viewModelScope.launch {
        try {
            // si ya tienes gastosUsers cargado globalmente, esto puede no hacer falta
            val gastosUsers = repo.getGastosUsers()
            _uiState.update { it.copy(gastosUsers = gastosUsers, errorMessage = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.message) }
        }
    }

    fun addIntegranteToGasto(gastoId: Int, userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.addGastoUser(GastoUser(gastoId = gastoId, userId = userId))
                val updated = repo.getGastosUsers()
                _uiState.update { it.copy(gastosUsers = updated, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val users = repo.getUsers()
                val grupos = repo.getGrupos()
                val gastos = repo.getGastos()
                val gastosUsers = repo.getGastosUsers()
                _uiState.update {
                    it.copy(
                        users = users,
                        grupos = grupos,
                        gastos = gastos,
                        gastosUsers = gastosUsers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}

