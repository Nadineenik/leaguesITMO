package com.example.leaguesitmo.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leaguesitmo.data.ApiService
import com.example.leaguesitmo.data.LeaguesDto
import com.example.leaguesitmo.data.toItem

import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val api = ApiService.create()

    private val _state = mutableStateOf<UiState>(UiState.Loading)
    val state: State<UiState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val result = api.getItems("48gaewnwzrk0o0hv")
                val items = result.map { it.toItem() }
                _state.value = UiState.Success(items)
            } catch (e: Exception) {
                _state.value = UiState.Error("Не удалось загрузить данные")
            }
        }
    }
}
