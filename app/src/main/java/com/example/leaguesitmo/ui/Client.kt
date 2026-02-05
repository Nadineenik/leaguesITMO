package com.example.leaguesitmo.ui


import com.example.leaguesitmo.data.LeaguesDto
import com.example.leaguesitmo.model.Item
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object Client {
    private const val API_KEY = "48gaewnwzrk0o0hv"
    private const val BASE_URL = "https://sstats.net/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: SerpStackApi = retrofit.create(SerpStackApi::class.java)

    suspend fun LeaguesDto.toItem(): Item {
        return Item(
            title = name,
            value = country.name
        )
}
}