package com.example.hiato.data

import com.example.hiato.mvvm.model.Gasto
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
}

