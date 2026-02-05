package com.example.leaguesitmo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.leaguesitmo.model.Item


@Composable
fun MainScreen(viewModel: MainViewModel = viewModel(), navController: NavController) {

    val state by viewModel.state

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }

            is UiState.Success -> {
                val list = (state as UiState.Success).data
                LazyColumn {
                    items(list) { item ->
                        ItemRow(item)
                    }
                }
            }

            is UiState.Error -> {
                Text(
                    text = (state as UiState.Error).message,
                    color = Color.Red
                )
            }
        }
    }
}
@Composable
fun ItemRow(item: Item) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = item.title,
            fontWeight = FontWeight.Bold
        )
        Text(text = item.value)
    }
}
