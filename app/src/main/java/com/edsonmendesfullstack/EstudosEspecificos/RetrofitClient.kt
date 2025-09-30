package com.edsonmendesfullstack.EstudosEspecificos

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 🚨 IMPORTANTE: Mude para a URL base da sua API do Render!
    private const val BASE_URL = "https://eeappads.onrender.com/"

    // Configura o cliente OkHttp com Timeouts longos para o Render
    private val okHttpClient = OkHttpClient.Builder()
        // 🚨 Define o timeout de conexão e de leitura para 40 segundos
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .build()

    // Inicializa e configura a instância Retrofit
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Usa o cliente configurado com timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}