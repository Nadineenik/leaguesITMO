// FiltersViewModel.kt
package com.example.leaguesitmo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leaguesitmo.data.ApiService
import com.example.leaguesitmo.data.SavedSearch
import com.example.leaguesitmo.data.SavedSearchDao
import com.example.leaguesitmo.model.MatchItem
import com.example.leaguesitmo.model.toMatchItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class FiltersViewModel(
    private val api: ApiService = ApiService.create(),
    private val savedSearchDao: SavedSearchDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 = предстоящие, 1 = архив
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Добавлены состояния для фильтрации по дате
    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _dateFilterType = MutableStateFlow(0) // 0 = сегодня, 1 = завтра, 2 = неделя, 3 = месяц, 4 = все даты
    val dateFilterType: StateFlow<Int> = _dateFilterType.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<String, String>?>(null)
    val dateRange: StateFlow<Pair<String, String>?> = _dateRange.asStateFlow()

    private val _filteredMatches = MutableStateFlow<List<MatchItem>>(emptyList())
    val filteredMatches: StateFlow<List<MatchItem>> = _filteredMatches.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            _recentSearches.value = savedSearchDao.getLast10().map { it.query }
        }
        // Устанавливаем фильтр "сегодня" по умолчанию
        setTodayFilter()
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        performSearch()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        performSearch()
    }

    fun applyRecentSearch(query: String) {
        _searchQuery.value = query
        performSearch()
    }

    // Новые методы для фильтрации по дате
    fun setTodayFilter() {
        _dateFilterType.value = 0
        val today = LocalDate.now().toString()
        _selectedDate.value = today
        _dateRange.value = null
        performSearch()
    }

    fun setTomorrowFilter() {
        _dateFilterType.value = 1
        val tomorrow = LocalDate.now().plusDays(1).toString()
        _selectedDate.value = tomorrow
        _dateRange.value = null
        performSearch()
    }

    fun setWeekFilter() {
        _dateFilterType.value = 2
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(6) // +6 дней = неделя
        _selectedDate.value = null
        _dateRange.value = startDate.toString() to endDate.toString()
        performSearch()
    }

    fun setMonthFilter() {
        _dateFilterType.value = 3
        val startDate = LocalDate.now()
        val endDate = startDate.plusMonths(1).minusDays(1) // +1 месяц -1 день
        _selectedDate.value = null
        _dateRange.value = startDate.toString() to endDate.toString()
        performSearch()
    }

    fun clearDateFilter() {
        _dateFilterType.value = 4
        _selectedDate.value = null
        _dateRange.value = null
        performSearch()
    }

    private fun performSearch() {
        val query = _searchQuery.value.trim().lowercase()

        // Если запрос пустой и нет фильтра по дате, очищаем результаты
        if (query.isEmpty() && _dateFilterType.value == 4) {
            _filteredMatches.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Сохраняем поисковый запрос
                if (query.isNotBlank()) {
                    savedSearchDao.insert(SavedSearch(query = query, timestamp = System.currentTimeMillis()))
                    _recentSearches.value = savedSearchDao.getLast10().map { it.query }
                }

                // Определяем статус в зависимости от вкладки
                // 0 = предстоящие (статус не 8), 1 = архив (статус 8)
                val statusParam = if (_selectedTab.value == 0) {
                    // Для предстоящих используем null, чтобы получить все НЕ завершенные матчи
                    // Можно также использовать "!8" если API поддерживает, но обычно null означает "не завершенные"
                    null
                } else {
                    // Для архива используем статус 8 (завершенные)
                    "8"
                }

                // Определяем дату(ы) для запроса в зависимости от типа фильтра
                val dateParam = when (_dateFilterType.value) {
                    0 -> LocalDate.now().toString() // Сегодня
                    1 -> LocalDate.now().plusDays(1).toString() // Завтра
                    2, 3 -> null // Для недели и месяца используем диапазон дат
                    else -> null // Все даты (без фильтра по дате)
                }

                Log.d("SearchRequest", "Запрос: вкладка=${if (_selectedTab.value == 0) "предстоящие" else "архив"}, date=$dateParam, status=$statusParam, limit=200")

                // Получаем матчи
                val matches = if (_dateFilterType.value == 2 || _dateFilterType.value == 3) {
                    // Для недели и месяца загружаем матчи за диапазон дат
                    val dateRange = _dateRange.value
                    if (dateRange != null) {
                        loadMatchesForDateRange(dateRange.first, dateRange.second, statusParam)
                    } else {
                        emptyList()
                    }
                } else if (dateParam != null) {
                    // Запрос с фильтром по дате
                    api.getGamesList(
                        date = dateParam,
                        status = statusParam,
                        limit = 200
                    ).data
                } else {
                    // Запрос без фильтра по дате
                    api.getGamesList(
                        status = statusParam,
                        limit = 200
                    ).data
                }

                Log.d("Search", "Загружено матчей от API: ${matches.size}")

                // Фильтруем по поисковому запросу и статусу
                val items = matches
                    .mapNotNull { dto -> dto.toMatchItem() }
                    .filter { match ->
                        // Фильтр по текстовому запросу
                        val matchesQuery = query.isEmpty() ||
                                match.homeTeam.lowercase().contains(query) ||
                                match.awayTeam.lowercase().contains(query) ||
                                match.league.lowercase().contains(query) ||
                                match.country?.lowercase()?.contains(query) == true

                        // Дополнительная фильтрация по статусу на случай, если API не фильтрует правильно
                        if (_selectedTab.value == 0) {
                            // Предстоящие: не завершенные
                            matchesQuery && !match.isFinished
                        } else {
                            // Архив: завершенные
                            matchesQuery && match.isFinished
                        }
                    }

                Log.d("Search", "После фильтра найдено: ${items.size} матчей")

                _filteredMatches.value = items

                if (items.isEmpty()) {
                    val periodText = when (_dateFilterType.value) {
                        0 -> "на сегодня"
                        1 -> "на завтра"
                        2 -> "на неделю"
                        3 -> "на месяц"
                        else -> ""
                    }
                    _error.value = "Ничего не найдено ${if (periodText.isNotEmpty()) periodText else ""} по запросу «$_searchQuery.value»"
                }

            } catch (e: Exception) {
                Log.e("Search", "Ошибка при поиске", e)
                _error.value = "Ошибка поиска: ${e.localizedMessage ?: "Неизвестная ошибка"}"
                if (e is retrofit2.HttpException) {
                    _error.value = "Сервер вернул ошибку ${e.code()}. Попробуйте другой запрос."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Метод для загрузки матчей за диапазон дат
    private suspend fun loadMatchesForDateRange(
        dateFrom: String,
        dateTo: String,
        status: String?
    ): List<com.example.leaguesitmo.data.dto.MatchDto> {
        val start = LocalDate.parse(dateFrom)
        val end = LocalDate.parse(dateTo)

        val allMatches = mutableListOf<com.example.leaguesitmo.data.dto.MatchDto>()

        var currentDate = start
        while (!currentDate.isAfter(end)) {
            try {
                val response = api.getGamesList(
                    date = currentDate.toString(),
                    status = status,
                    limit = 200
                )
                allMatches.addAll(response.data)
                Log.d("DateRange", "Загружено матчей за $currentDate: ${response.data.size}")
            } catch (e: Exception) {
                Log.e("DateRange", "Ошибка загрузки для даты $currentDate", e)
            }
            currentDate = currentDate.plusDays(1)
        }

        return allMatches
    }
}