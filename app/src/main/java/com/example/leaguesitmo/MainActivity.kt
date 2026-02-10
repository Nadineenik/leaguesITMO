package com.example.leaguesitmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.leaguesitmo.navigation.SetupNavGraph
import com.example.leaguesitmo.ui.AuthState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val authState: AuthState = viewModel()  // ← работает в Activity, потому что есть Activity контекст
            SetupNavGraph(authState = authState)
        }
    }
}