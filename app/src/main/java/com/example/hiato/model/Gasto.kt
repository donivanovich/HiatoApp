package com.example.hiato.model

data class Gasto(
    val id: Int? = null,
    val grupoId: Int,
    val nombre: String,
    val precio: Double
)