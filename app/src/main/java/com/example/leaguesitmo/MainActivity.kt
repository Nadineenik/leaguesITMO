package com.example.leaguesitmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider

import com.example.leaguesitmo.navigation.SetupNavGraph
import com.example.leaguesitmo.ui.AuthState


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authState = ViewModelProvider(this)[AuthState::class.java]

        setContent {
            val context = LocalContext.current
            SetupNavGraph(authState = authState)

        }
    }
}