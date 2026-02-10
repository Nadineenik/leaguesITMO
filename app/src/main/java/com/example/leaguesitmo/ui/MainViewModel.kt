package com.example.leaguesitmo.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leaguesitmo.data.ApiService
import com.example.leaguesitmo.data.dto.MatchesResponse
import com.example.leaguesitmo.model.MatchItem
import com.example.leaguesitmo.model.toMatchItem
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class MainViewModel(private val context: Context) : ViewModel() {

    private val api = ApiService.create()

    private val _todayMatches = mutableStateOf<List<MatchItem>>(emptyList())
    val todayMatches: State<List<MatchItem>> = _todayMatches

    private val _tomorrowMatches = mutableStateOf<List<MatchItem>>(emptyList())
    val tomorrowMatches: State<List<MatchItem>> = _tomorrowMatches

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (!isNetworkAvailable(context)) {
                    _error.value = "Нет интернета. Проверьте соединение."
                    Log.w("Matches", "Нет интернета")
                    return@launch
                }

                val today = LocalDate.now().toString()           // например "2026-02-09"
                val tomorrow = LocalDate.now().plusDays(1).toString()  // "2026-02-10"

                Log.d("Matches", "Запрос на сегодня: $today")
                Log.d("Matches", "Запрос на завтра: $tomorrow")

                // Загружаем все матчи без жёсткого фильтра по статусу
                val todayResp = api.getGamesList(date = today)
                val tomorrowResp = api.getGamesList(date = tomorrow)

                Log.d("Matches", "API вернул сегодня: ${todayResp.data.size} матчей")
                Log.d("Matches", "API вернул завтра:  ${tomorrowResp.data.size} матчей")

                // Преобразуем и фильтруем мягко
                val todayItems = todayResp.data
                    .mapNotNull { dto ->
                        try {
                            val item = dto.toMatchItem()
                            // Логируем проблемные матчи для отладки
                            if (item.homeTeam == "—" && item.awayTeam == "—") {
                                Log.w("Matches", "Пустой матч сегодня: id=${dto.id}")
                            }
                            item
                        } catch (e: Exception) {
                            Log.w("Matches", "Ошибка в toMatchItem() для матча ${dto.id}", e)
                            null
                        }
                    }
                    .filter { item ->
                        // Показываем, если хотя бы одна команда известна и есть осмысленное название
                        (item.homeTeam.isNotBlank() && item.homeTeam != "—") ||
                                (item.awayTeam.isNotBlank() && item.awayTeam != "—")
                    }

                val tomorrowItems = tomorrowResp.data
                    .mapNotNull { dto ->
                        try {
                            val item = dto.toMatchItem()
                            if (item.homeTeam == "—" && item.awayTeam == "—") {
                                Log.w("Matches", "Пустой матч завтра: id=${dto.id}")
                            }
                            item
                        } catch (e: Exception) {
                            Log.w("Matches", "Ошибка в toMatchItem() для матча ${dto.id}", e)
                            null
                        }
                    }
                    .filter { item ->
                        (item.homeTeam.isNotBlank() && item.homeTeam != "—") ||
                                (item.awayTeam.isNotBlank() && item.awayTeam != "—")
                    }

                Log.d("Matches", "После обработки сегодня: ${todayItems.size} матчей")
                Log.d("Matches", "После обработки завтра:  ${tomorrowItems.size} матчей")

                _todayMatches.value = todayItems
                _tomorrowMatches.value = tomorrowItems

                if (todayItems.isEmpty() && tomorrowItems.isEmpty()) {
                    _error.value = "Матчи на выбранные даты не найдены"
                }

            } catch (e: Exception) {
                Log.e("Matches", "Критическая ошибка при загрузке", e)
                _error.value = "Ошибка загрузки: ${e.localizedMessage ?: "Неизвестная ошибка"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    // Для pull-to-refresh, если добавите
    fun refresh() {
        loadMatches()
    }
}