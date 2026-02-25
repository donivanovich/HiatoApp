package com.example.hiato

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
}
