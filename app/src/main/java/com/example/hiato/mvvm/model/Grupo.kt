package com.example.hiato.mvvm.model

import com.google.gson.annotations.SerializedName

data class Grupo(
    val id: Int? = null,
    val nombre: String,
    @SerializedName("user_id")
    val userId: Int
)