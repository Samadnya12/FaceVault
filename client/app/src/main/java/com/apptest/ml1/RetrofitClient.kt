package com.apptest.ml1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.apptest.ml1.ApiService

object RetrofitClient {
    private const val BASE_URL ="http://192.168.0.101:8000/"

    val api : ApiService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}