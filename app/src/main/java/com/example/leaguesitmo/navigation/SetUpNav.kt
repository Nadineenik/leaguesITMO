package com.example.leaguesitmo.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.leaguesitmo.ui.AuthState
import com.example.leaguesitmo.ui.DetailScreen
import com.example.leaguesitmo.ui.FiltersScreen
import com.example.leaguesitmo.ui.HistoryScreen
import com.example.leaguesitmo.ui.LoginScreen
import com.example.leaguesitmo.ui.MainScreen
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Вход", Icons.Filled.Lock)
    object Main : Screen("main", "Главное", Icons.Filled.Search)
    object Filters : Screen("filters", "Фильтры", Icons.Filled.Build)
    // В navigation/Screen.kt изменим:
    object Detail : Screen("detail/{matchId}/{matchJson}", "Детали матча") {
        fun createRoute(matchId: String, matchJson: String): String =
            "detail/${matchId}/${Uri.encode(matchJson)}"
    }
    object History : Screen("history", "История", Icons.Filled.Star)
}

@Composable
fun SetupNavGraph(
    authState: AuthState,
    navController: NavHostController = rememberNavController()
) {
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authState.isLoggedIn.collectLatest { value ->
            isLoggedIn = value
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = { BottomNavBar(navController, isLoggedIn, currentRoute) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authState = authState,
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Main.route) {
                MainScreen(navController = navController)
            }

            composable(Screen.Filters.route) {
                FiltersScreen(navController = navController)
            }


            composable(
                route = "detail/{matchId}/{matchJson}",
                arguments = listOf(
                    navArgument("matchId") { type = NavType.IntType },
                    navArgument("matchJson") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
                val matchJson = backStackEntry.arguments?.getString("matchJson") ?: ""

                // Декодируем JSON
                val decodedJson = java.net.URLDecoder.decode(matchJson, "UTF-8")

                DetailScreen(
                    matchId = matchId,
                    initialMatchJson = decodedJson,
                    navController = navController
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(navController = navController)
            }
        }
    }
}

@Composable
fun BottomNavBar(
    navController: NavHostController,
    isLoggedIn: Boolean,
    currentRoute: String?
) {
    val items = if (isLoggedIn) {
        listOf(Screen.Main, Screen.Filters)
    } else {
        listOf(Screen.Main, Screen.Filters, Screen.Login)
    }

    NavigationBar {
        items.forEach { screen ->
            val isSelected = when {
                currentRoute == screen.route -> true
                currentRoute?.startsWith("detail/") == true && screen == Screen.Main -> true
                else -> false
            }

            NavigationBarItem(
                icon = {
                    screen.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = screen.title,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = true
            )
        }
    }
}