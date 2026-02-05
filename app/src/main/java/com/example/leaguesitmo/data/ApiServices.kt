package com.example.leaguesitmo.data
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("leagues")
    suspend fun getItems(
        @Header("Authorization") token: String
    ): List<LeaguesDto>

    @GET("games/{id}")
    suspend fun getGame(
        @Path("id") id: Int
    )
    @GET("games")
    suspend fun getStats(
        @Query("season") season: Int,
        @Query("team") teamId: Int
    )

    companion object {
        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl("https://sstats.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
