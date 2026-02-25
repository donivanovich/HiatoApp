package com.example.hiato

import com.example.hiato.model.Gasto
import com.example.hiato.model.GastoUser
import com.example.hiato.model.Grupo
import com.example.hiato.model.User
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
