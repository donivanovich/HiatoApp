package com.example.hiato.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.data.HiatoRepository
import com.example.hiato.mvvm.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AmigosUiState(
    val amigos: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AmigosViewModel(
    private val repository: HiatoRepository = HiatoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmigosUiState(isLoading = true))
    val uiState: StateFlow<AmigosUiState> = _uiState.asStateFlow()

    fun loadAmigos(userId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val allUsers = repository.getUsers()
                val filteredAmigos = allUsers.filter { it.id != userId }
                _uiState.value = AmigosUiState(
                    amigos = filteredAmigos,
                    isLoading = false
                )
                println("AmigosViewModel: ${filteredAmigos.size} amigos cargados para userId=$userId")
            } catch (e: Exception) {
                _uiState.value = AmigosUiState(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                println("Error cargando amigos: ${e.message}")
            }
        }
    }
}
