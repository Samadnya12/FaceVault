package com.apptest.ml1

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/register")
    fun register(@Body request: FaceRequest): Call<ApiResponse>

    @POST("/login")
    fun login(@Body request: FaceRequest): Call<ApiResponse>
}

