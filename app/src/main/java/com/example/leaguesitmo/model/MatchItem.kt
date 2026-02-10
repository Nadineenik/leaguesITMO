package com.example.leaguesitmo.model

import com.example.leaguesitmo.data.dto.MatchDto
import com.example.leaguesitmo.data.dto.MatchDetailData  // ← правильный импорт для MatchDetailData
import java.text.SimpleDateFormat
import java.util.Locale

data class MatchItem(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val country: String?,
    val time: String,
    val venue: String? = null,
    val score: String? = null,
    val isFinished: Boolean
) {
    val vsText: String get() = "$homeTeam — $awayTeam"

    val leagueAndCountry: String
        get() = buildString {
            append(league)
            if (!country.isNullOrBlank()) append(" • $country")
        }
}

// Конвертер для списка матчей (Games/list)
fun MatchDto.toMatchItem(): MatchItem {
    val isFinished = status == 8 ||
            statusName?.lowercase()?.let { it.contains("fin") || it == "ft" } == true

    val timeFormatted = formatDateTime(date)

    val scoreText = when {
        isFinished -> {
            val home = homeFTResult ?: homeResult ?: "?"
            val away = awayFTResult ?: awayResult ?: "?"
            "$home : $away"
        }
        else -> null
    }

    return MatchItem(
        id = id ?: 0,
        homeTeam = homeTeam?.name ?: "—",
        awayTeam = awayTeam?.name ?: "—",
        league = season?.league?.name ?: "Неизвестная лига",
        country = season?.league?.country?.name,
        time = timeFormatted,
        venue = null,  // в списке venue обычно нет
        score = scoreText,
        isFinished = isFinished
    )
}

fun MatchDetailData.toMatchItem(): MatchItem {
    val isFinished = status == 8 ||
            statusName?.lowercase()?.let { it.contains("fin") || it == "ft" || it.contains("ended") } == true

    val timeFormatted = formatDateTime(date) ?: "Время неизвестно"

    val scoreText = if (isFinished) {
        val home = homeFTResult ?: homeResult ?: homeHTResult ?: "?"
        val away = awayFTResult ?: awayResult ?: awayHTResult ?: "?"
        "$home : $away"
    } else null

    return MatchItem(
        id = id ?: 0,
        homeTeam = homeTeam?.name ?: "Команда неизвестна",
        awayTeam = awayTeam?.name ?: "Команда неизвестна",
        league = season?.league?.name ?: "Лига неизвестна",
        country = season?.league?.country?.name ?: "—",
        time = timeFormatted,
        venue = venue?.let { v ->
            if (v.city != null) "${v.name} (${v.city})" else v.name
        } ?: "Стадион неизвестен",
        score = scoreText,
        isFinished = isFinished
    )
}
private fun formatDateTime(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "—"

    val outputFormat = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale("ru", "RU"))

    listOf(
        "yyyy-MM-dd'T'HH:mm:ssXXX",      // основной формат от API
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",  // с миллисекундами
        "yyyy-MM-dd HH:mm:ss"            // иногда без T и Z
    ).forEach { pattern ->
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.parse(rawDate)?.let { return outputFormat.format(it) }
        } catch (_: Throwable) {}
    }

    return rawDate.take(16).replace("T", " ") // fallback
}