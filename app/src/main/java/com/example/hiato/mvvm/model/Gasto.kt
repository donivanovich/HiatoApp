package com.example.hiato.mvvm.model

data class Gasto(
    val grupoId: Int,
    val id: Int? = null,
    val nombre: String,
    val precio: Double
)