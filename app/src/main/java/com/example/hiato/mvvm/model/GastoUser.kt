package com.example.hiato.mvvm.model

import com.google.gson.annotations.SerializedName

data class GastoUser(
    val id: Int? = null,
    @SerializedName("gasto_id")
    val gastoId: Int,
    @SerializedName("user_id")
    val userId: Int
)
