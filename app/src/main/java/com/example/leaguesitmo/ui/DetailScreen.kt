package com.example.leaguesitmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.leaguesitmo.model.MatchItem
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    matchId: Int,
    initialMatchJson: String? = null,
    navController: NavController
) {
    // Пытаемся распарсить initialMatch из JSON
    val initialMatch = remember(initialMatchJson) {
        initialMatchJson?.let { json ->
            try {
                Gson().fromJson(json, MatchItem::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    val viewModel: MatchDetailViewModel = viewModel(
        factory = MatchDetailViewModel.Factory(matchId)
    )

    val apiMatchState by viewModel.match.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Используем данные из API или fallback на initialMatch
    val match = apiMatchState ?: initialMatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (match != null) "Детали матча" else "Матч #$matchId"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Показываем ID матча для отладки
            if (match == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Загрузка данных...")
                        } else {
                            Text(
                                text = "Не удалось загрузить данные матча",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ID: $matchId",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (error != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ошибка: $error",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                // Информация о источнике данных
                if (apiMatchState == null && initialMatch != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "⚠️ Показаны данные из списка матчей",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF57C00),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Основное содержимое
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Заголовок с командами
                    Text(
                        text = match.vsText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Лига и страна
                    Text(
                        text = match.leagueAndCountry,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Дата и время
                    Text(
                        text = match.time,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Счет или статус
                    if (match.score != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = match.score,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (match.isFinished) {
                                    Text(
                                        text = "ЗАВЕРШЁН",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "ЗАПЛАНИРОВАН",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Матч ещё не начался",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Дополнительная информация
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        // Статус
                        DetailRow(
                            label = "Статус",
                            value = if (match.isFinished) "Завершён" else "Запланирован"
                        )

                        // Стадион (если есть)
                        if (!match.venue.isNullOrBlank()) {
                            DetailRow(
                                label = "Стадион",
                                value = match.venue!!
                            )
                        } else {
                            DetailRow(
                                label = "Стадион",
                                value = "Информация отсутствует"
                            )
                        }

                        // ID матча (для отладки)
                        DetailRow(
                            label = "ID матча",
                            value = match.id.toString()
                        )

                        // Источник данных
                        DetailRow(
                            label = "Источник",
                            value = if (apiMatchState != null) "API деталей" else "Список матчей"
                        )
                    }

                    // Дополнительное сообщение, если данные из списка
                    if (apiMatchState == null && initialMatch != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "ℹ️ Информация",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Детальная информация о матче временно недоступна через API. " +
                                            "Показаны данные из общего списка матчей.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "В списке матчей нет информации о: стадионе, судье, зрителях и других деталях.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1.5f)
                .padding(start = 16.dp)
        )
    }
    Divider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}