package com.example.leaguesitmo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leaguesitmo.data.ApiService
import com.example.leaguesitmo.data.dto.MatchDetailDto
import com.example.leaguesitmo.model.MatchItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.leaguesitmo.model.toMatchItem

class MatchDetailViewModel(
    private val matchId: Int,
    private val api: ApiService = ApiService.create()
) : ViewModel() {

    private val _match = MutableStateFlow<MatchItem?>(null)
    val match: StateFlow<MatchItem?> = _match

    private val _isLoading = MutableStateFlow(false) // Начинаем с false, чтобы не мешать отображению
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Загружаем в фоне, не блокируя UI
        loadDetailInBackground()
    }

    fun reload() {
        loadDetailInBackground()
    }

    private fun loadDetailInBackground() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("MatchDetail", "Фоновая загрузка деталей для matchId: $matchId")
                val response: MatchDetailDto = api.getMatchDetail(id = matchId)

                Log.d("MatchDetail", "Статус ответа: ${response.status}")

                // Принимаем любой статус, который не является ошибкой
                if (response.status.equals("success", ignoreCase = true) ||
                    response.status.equals("OK", ignoreCase = true) ||
                    response.status.equals("ok", ignoreCase = true)) {

                    if (response.data != null) {
                        // Проверяем, есть ли хоть какие-то данные
                        val hasData = response.data.id != null ||
                                response.data.homeTeam != null ||
                                response.data.awayTeam != null ||
                                response.data.date != null

                        if (hasData) {
                            val matchItem = response.data.toMatchItem()
                            Log.d("MatchDetail", "Загружены данные из API: $matchItem")
                            _match.value = matchItem
                        } else {
                            Log.d("MatchDetail", "API вернул пустой объект данных")
                            _error.value = "Детальная информация недоступна"
                        }
                    } else {
                        Log.d("MatchDetail", "API вернул data=null")
                        _error.value = "Детальная информация отсутствует"
                    }
                } else {
                    Log.d("MatchDetail", "Неудачный статус: ${response.status}")
                    _error.value = "Статус API: ${response.status}"
                }
            } catch (e: Exception) {
                Log.e("MatchDetail", "Ошибка загрузки", e)
                _error.value = "Ошибка соединения"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val matchId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MatchDetailViewModel(matchId) as T
        }
    }
}