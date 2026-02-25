package com.example.hiato.data

import com.example.hiato.mvvm.model.Gasto
import com.example.hiato.mvvm.model.GastoUser
import com.example.hiato.mvvm.model.Grupo
import com.example.hiato.mvvm.model.User
import retrofit2.http.GET

interface HiatoApi {
    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("grupos")
    suspend fun getGrupos(): List<Grupo>

    @GET("gastos")
    suspend fun getGastos(): List<Gasto>

    @GET("gastos_users")
    suspend fun getGastosUsers(): List<GastoUser>
}
