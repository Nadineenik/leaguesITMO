package com.example.leaguesitmo.data

data class LeaguesDto(
    val id: Int,
    val name: String,
    val country: CountryDto
)
data class CountryDto(
    val code: String,
    val name: String
)