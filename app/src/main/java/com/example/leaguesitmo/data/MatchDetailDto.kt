package com.example.leaguesitmo.data.dto

import com.google.gson.annotations.SerializedName

// Обновленная структура на основе документации
data class MatchDetailDto(
    @SerializedName("status")
    val status: String,

    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("data")
    val data: MatchDetailData? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("offset")
    val offset: Int? = null,

    @SerializedName("TotalCount")
    val totalCount: Int? = null,

    @SerializedName("traceId")
    val traceId: String? = null,

    @SerializedName("requestQuery")
    val requestQuery: String? = null
)

data class MatchDetailData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("flashId")
    val flashId: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("dateUtc")
    val dateUtc: Long? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("statusName")
    val statusName: String? = null,

    @SerializedName("elapsed")
    val elapsed: Int? = null,

    @SerializedName("extraMinutes")
    val extraMinutes: Int? = null,

    @SerializedName("homeResult")
    val homeResult: Int? = null,

    @SerializedName("awayResult")
    val awayResult: Int? = null,

    @SerializedName("homeHTResult")
    val homeHTResult: Int? = null,

    @SerializedName("awayHTResult")
    val awayHTResult: Int? = null,

    @SerializedName("homeFTResult")
    val homeFTResult: Int? = null,

    @SerializedName("awayFTResult")
    val awayFTResult: Int? = null,

    @SerializedName("homeTeam")
    val homeTeam: TeamDto? = null,

    @SerializedName("awayTeam")
    val awayTeam: TeamDto? = null,

    @SerializedName("season")
    val season: SeasonDto? = null,

    @SerializedName("roundName")
    val roundName: String? = null,

    @SerializedName("venue")
    val venue: VenueDto? = null,

    @SerializedName("odds")
    val odds: List<OddDto>? = null
)

data class OddDto(
    @SerializedName("marketId")
    val marketId: Int? = null,

    @SerializedName("marketName")
    val marketName: String? = null,

    @SerializedName("odds")
    val odds: List<OddValueDto>? = null
)

data class OddValueDto(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("value")
    val value: Double? = null,

    @SerializedName("openingValue")
    val openingValue: Double? = null
)

data class VenueDto(
    val id: Int? = null,
    val name: String? = null,
    val city: String? = null,
    val country: CountryDto? = null
)