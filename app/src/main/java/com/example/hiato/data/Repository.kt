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
        .baseUrl("http://10.0.2.2:8000/")  // Emulador
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build())
        .build()

    private val api = retrofit.create(HiatoApi::class.java)

    suspend fun getUsers() = api.getUsers()
    suspend fun getGrupos() = api.getGrupos()
    suspend fun getGastos() = api.getGastos()

    suspend fun getGastosUsers() = api.getGastosUsers()

    suspend fun getGastosByGrupo(grupoId: Int): List<Gasto> {
        val todosGastos = getGastos()
        return todosGastos.filter { it.grupoId == grupoId }
    }

    suspend fun updateUser(id: Int, user: User): User = api.updateUser(id, user)

    suspend fun addGasto(grupoId: Int, nombre: String, precio: Double): Gasto {
        val gasto = Gasto(
            grupoId = grupoId,
            id = null,  // El backend lo genera
            nombre = nombre,
            precio = precio
        )
        return api.createGasto(gasto)
    }

    suspend fun addGrupo(userId: Int, nombre: String): Grupo {
        val grupo = Grupo(
            id = null,  // Backend genera
            nombre = nombre,
            userId = userId
        )
        return api.createGrupo(grupo)
    }

    suspend fun addGastoUser(gastoUser: GastoUser): GastoUser = api.createGastoUser(gastoUser)

    suspend fun getGastosUsersFresh(): List<GastoUser> {
        val timestamp = System.currentTimeMillis()
        // Llama tu endpoint existente con header no-cache
        return api.getGastosUsers()
    }

}

