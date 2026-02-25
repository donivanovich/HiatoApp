package com.example.hiato.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiato.HiatoRepository
import com.example.hiato.model.Gasto
import com.example.hiato.model.Grupo
import com.example.hiato.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repo = HiatoRepository()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _grupos = MutableStateFlow<List<Grupo>>(emptyList())
    val grupos: StateFlow<List<Grupo>> = _grupos

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _users.value = repo.getUsers()
            _grupos.value = repo.getGrupos()
            _gastos.value = repo.getGastos()
        }
    }
}
