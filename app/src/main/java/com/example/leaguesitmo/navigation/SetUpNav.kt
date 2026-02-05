package com.example.leaguesitmo.navigation


import com.example.leaguesitmo.ui.AuthState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.leaguesitmo.ui.MainScreen
import kotlinx.coroutines.flow.collectLatest
//import nadinee.studentmaterialssearch.screens.*
import java.net.URLDecoder
import java.net.URLEncoder


sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Вход", Icons.Filled.Lock)
    object Main : Screen("main", "Главное", Icons.Filled.Search)
    object Filters : Screen("filters", "Фильтры", Icons.Filled.Build)
    object Details : Screen(
        route = "details/{url}/{title}/{content}",
        title = "Maтч"
    ) {
        fun createRoute(url: String, title: String, content: String): String {
            return "details/" +
                    "${URLEncoder.encode(url, "UTF-8")}/" +
                    "${URLEncoder.encode(title, "UTF-8")}/" +
                    "${URLEncoder.encode(content, "UTF-8")}"
        }
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
                LoginScreen(authState = authState, onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Main.route) {
                MainScreen(navController = navController, authState = authState)
            }

            composable(Screen.Filters.route) {
                FiltersScreen(navController = navController, authState = authState)
            }


            composable(
                Screen.Details.route,
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                val url = try { URLDecoder.decode(encodedUrl, "UTF-8") } catch (e: Exception) { encodedUrl }
                DetailsScreen(navController = navController, authState = authState)
            }


            composable(Screen.History.route) {
                HistoryScreen(navController = navController, authState = authState)
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
            val selected = when {
                currentRoute?.startsWith("details") == true -> screen == Screen.Main
                currentRoute?.startsWith("webview") == true -> screen == Screen.Main
                else -> currentRoute == screen.route
            }

            NavigationBarItem(
                icon = {
                    screen.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = screen.title,
                            modifier = Modifier.size(20.dp) // чуть меньше иконки
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,           // ← УМЕНЬШЕННЫЙ ШРИФТ!
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                // Делаем чуть компактнее
                alwaysShowLabel = true
            )
        }
    }
}