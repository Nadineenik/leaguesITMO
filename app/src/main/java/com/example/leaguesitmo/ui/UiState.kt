package com.example.leaguesitmo.ui

import com.example.leaguesitmo.model.Item

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
}
