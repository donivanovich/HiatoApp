package com.example.hiato.data

import com.example.hiato.mvvm.model.Gasto
import com.example.hiato.mvvm.model.GastoUser
import com.example.hiato.mvvm.model.Grupo
import com.example.hiato.mvvm.model.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class HiatoRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build())
        .build()

    private val api = retrofit.create(HiatoApi::class.java)

    suspend fun getUsers() = api.getUsers() //Metodo para cargar Usuarios

    suspend fun getGrupos() = api.getGrupos() //Metodo para cargar Grupos

    suspend fun getGastos() = api.getGastos() //Metodo para cargar Gastos

    suspend fun getGastosUsers() = api.getGastosUsers() //Metodo para cargar Integrantes

    suspend fun updateUser(id: Int, user: User): User = api.updateUser(id, user) //Metodo para editar el Usuario Logeado

    suspend fun createUser(email: String, nombre: String, password: String): User {
        val user = User(
            id = null,
            email = email,
            nombre = nombre,
            password = password
        )
        return api.createUser(user)
    } //Metodo para crear nuevo Usuario

    suspend fun addGrupo(userId: Int, nombre: String): Grupo {
        val grupo = Grupo(
            id = null,
            nombre = nombre,
            userId = userId
        )
        return api.createGrupo(grupo)
    } //Metodo para crear un Grupo

    suspend fun addGasto(grupoId: Int, nombre: String, precio: Double): Gasto {
        val gasto = Gasto(
            grupoId = grupoId,
            id = null,
            nombre = nombre,
            precio = precio
        )
        return api.createGasto(gasto)
    } //Metodo para crear un Gasto

    suspend fun addGastoUser(gastoId: Int, userId: Int): GastoUser {
        val gastoUser = GastoUser(
            id = null,
            gastoId = gastoId,
            userId = userId
        )
        return api.createGastoUser(gastoUser)
    } //Metodo para crear un Integrante
}

