// MatchDto.kt
package com.example.leaguesitmo.data.dto

import com.google.gson.annotations.SerializedName

data class MatchesResponse(
    val status: String,
    val count: Int,
    val data: List<MatchDto>
)

data class MatchDto(
    val id: Int? = null,
    val flashId: String? = null,
    val date: String? = null,              // "2026-02-08T21:00:00+00:00"
    val dateUtc: Long? = null,
    val status: Int? = null,               // 8 = Finished, 14 = Postponed и т.д.
    val statusName: String? = null,
    @SerializedName("homeResult")
    val homeResult: Int? = null,
    @SerializedName("awayResult")
    val awayResult: Int? = null,
    @SerializedName("homeFTResult")
    val homeFTResult: Int? = null,
    @SerializedName("awayFTResult")
    val awayFTResult: Int? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val season: SeasonDto? = null,
    val roundName: String? = null
)

data class TeamDto(
    val id: Int? = null,
    val name: String? = null,
    val flashId: String? = null,
    val country: CountryDto? = null
)

data class CountryDto(
    val code: String? = null,
    val name: String? = null
)

data class SeasonDto(
    val uid: String? = null,
    val year: Int? = null,
    val league: LeagueDto? = null
)

data class LeagueDto(
    val id: Int? = null,
    val name: String? = null,
    val country: CountryDto? = null,
    val flashScoreId: String? = null
)