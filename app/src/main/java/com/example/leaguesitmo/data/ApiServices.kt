package com.example.leaguesitmo.data

import com.example.leaguesitmo.data.dto.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.GsonBuilder

interface ApiService {

    @GET("Games/list")
    suspend fun getGamesList(
        @Header("Authorization") auth: String = "Bearer 48gaewnwzrk0o0hv",
        @Query("date") date: String? = null,
        @Query("dateFrom") dateFrom: String? = null, // Добавлено
        @Query("dateTo") dateTo: String? = null,     // Добавлено
        @Query("status") status: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100
    ): MatchesResponse

    @GET("Games/{id}")
    suspend fun getMatchDetail(
        @Header("Authorization") auth: String = "Bearer 48gaewnwzrk0o0hv",
        @Path("id") id: Int
    ): MatchDetailDto

    companion object {
        private const val BASE_URL = "https://api.sstats.net/"
        private const val TOKEN = "48gaewnwzrk0o0hv"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }
}