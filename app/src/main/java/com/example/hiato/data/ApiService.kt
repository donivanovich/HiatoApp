package com.example.hiato.data

import com.example.hiato.mvvm.model.Gasto
import com.example.hiato.mvvm.model.GastoUser
import com.example.hiato.mvvm.model.Grupo
import com.example.hiato.mvvm.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HiatoApi {
    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("grupos")
    suspend fun getGrupos(): List<Grupo>

    @GET("gastos")
    suspend fun getGastos(): List<Gasto>

    @GET("gastos_users")
    suspend fun getGastosUsers(): List<GastoUser>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): User

    @POST("gastos")
    suspend fun createGasto(@Body gasto: Gasto): Gasto

    @POST("grupos")
    suspend fun createGrupo(@Body grupo: Grupo): Grupo
}
