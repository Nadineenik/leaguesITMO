// FiltersScreen.kt
package com.example.leaguesitmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.leaguesitmo.App
import com.example.leaguesitmo.data.ApiService
import com.example.leaguesitmo.model.MatchItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(navController: NavController) {
    // Фабрика для ViewModel
    val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FiltersViewModel::class.java)) {
                return FiltersViewModel(
                    api = ApiService.create(),
                    savedSearchDao = App.database.savedSearchDao()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    val viewModel: FiltersViewModel = viewModel(factory = factory)

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateFilterType by viewModel.dateFilterType.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val matches by viewModel.filteredMatches.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск матчей") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { /* можно открыть полную историю */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "История поисков")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Вкладки
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Предстоящие") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Архив") }
                )
            }

            // Фильтры по дате (добавлено)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Сегодня", "Завтра", "Неделя", "Месяц", "Все даты")) { filter ->
                    val isSelected = when (filter) {
                        "Сегодня" -> dateFilterType == 0
                        "Завтра" -> dateFilterType == 1
                        "Неделя" -> dateFilterType == 2
                        "Месяц" -> dateFilterType == 3
                        "Все даты" -> dateFilterType == 4
                        else -> false
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            when (filter) {
                                "Сегодня" -> viewModel.setTodayFilter()
                                "Завтра" -> viewModel.setTomorrowFilter()
                                "Неделя" -> viewModel.setWeekFilter()
                                "Месяц" -> viewModel.setMonthFilter()
                                "Все даты" -> viewModel.clearDateFilter()
                            }
                        },
                        label = { Text(filter) }
                    )
                }
            }

            // Отображение выбранного периода (добавлено)
            when (dateFilterType) {
                0, 1 -> {
                    // Сегодня или завтра
                    if (selectedDate != null) {
                        Text(
                            text = "Дата: ${formatDateForDisplay(selectedDate ?: "")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                2 -> {
                    // Неделя
                    if (dateRange != null) {
                        Text(
                            text = "Период: ${formatDateForDisplay(dateRange!!.first)} - ${formatDateForDisplay(dateRange!!.second)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                3 -> {
                    // Месяц
                    if (dateRange != null) {
                        Text(
                            text = "Период: ${formatDateForDisplay(dateRange!!.first)} - ${formatDateForDisplay(dateRange!!.second)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Команда, лига или игрок") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Последние поиски (чипы)
            if (recentSearches.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentSearches) { query ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.applyRecentSearch(query) },
                            label = { Text(query) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Состояние загрузки / ошибка / результаты
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text(
                        text = error ?: "Неизвестная ошибка",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
                matches.isEmpty() -> {
                    Text(
                        text = "Ничего не найдено\nПопробуйте изменить запрос",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
                else -> {
                    LazyColumn {
                        items(matches, key = { it.id }) { match ->
                            MatchCard(
                                match = match,
                                onClick = {
                                    val gson = com.google.gson.Gson()
                                    val matchJson = gson.toJson(match)
                                    val encodedJson = java.net.URLEncoder.encode(matchJson, "UTF-8")
                                    navController.navigate("detail/${match.id}/$encodedJson")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательная функция для форматирования даты (добавлено)
private fun formatDateForDisplay(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}