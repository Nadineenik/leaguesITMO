package com.example.leaguesitmo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.leaguesitmo.model.MatchItem
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel

@Composable
fun MainScreen(
    navController: NavController
) {
    val context = LocalContext.current

    // Фабрика для ViewModel (оставляем как было)
    val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    val viewModel: MainViewModel = viewModel(factory = factory)

    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val todayMatches by viewModel.todayMatches
    val tomorrowMatches by viewModel.tomorrowMatches

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок и матчи "Сегодня"
        item {
            Text(
                text = "Сегодня",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        if (todayMatches.isEmpty()) {
            item {
                Text(
                    text = "Нет матчей на сегодня",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(todayMatches, key = { it.id }) { match ->
                MatchCard(
                    match = match,
                    onClick = {
                        Log.d("MainScreen", "Нажата карточка матча: id=${match.id}, home=${match.homeTeam}, away=${match.awayTeam}")
                        // Преобразуем MatchItem в JSON для передачи
                        val gson = com.google.gson.Gson()
                        val matchJson = gson.toJson(match)
                        // Кодируем JSON для безопасной передачи в URL
                        val encodedJson = java.net.URLEncoder.encode(matchJson, "UTF-8")
                        navController.navigate("detail/${match.id}/$encodedJson")
                    }
                )
            }
        }

        // Разделитель между секциями
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Заголовок и матчи "Завтра"
        item {
            Text(
                text = "Завтра",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (tomorrowMatches.isEmpty()) {
            item {
                Text(
                    text = "Нет матчей на завтра",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(tomorrowMatches, key = { it.id }) { match ->
                MatchCard(
                    match = match,
                    onClick = {
                        // Преобразуем MatchItem в JSON для передачи
                        val gson = com.google.gson.Gson()
                        val matchJson = gson.toJson(match)
                        // Кодируем JSON для безопасной передачи в URL
                        val encodedJson = java.net.URLEncoder.encode(matchJson, "UTF-8")
                        navController.navigate("detail/${match.id}/$encodedJson")
                    }
                )
            }
        }

        // Отступ снизу, чтобы нижняя навигация не перекрывала контент
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MatchCard(
    match: MatchItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (match.isFinished)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Первая строка: команды и счет
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.vsText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Лига и страна
                    Text(
                        text = match.leagueAndCountry,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Счет или статус
                if (match.score != null) {
                    Text(
                        text = match.score,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (match.isFinished) {
                    Text(
                        text = "FT",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            // Вторая строка: время и стадион
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = match.time,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                if (match.isFinished) {
                    Text(
                        text = "Завершён",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Стадион (если есть)
            if (!match.venue.isNullOrBlank()) {
                Text(
                    text = "📍 ${match.venue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}