package com.facucastro.focusguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.facucastro.focusguard.presentation.core.presentation.core.navigation.AppRoute
import com.facucastro.focusguard.presentation.core.presentation.core.navigation.MainTab
import com.facucastro.focusguard.presentation.core.presentation.core.navigation.TopLevelBackStack
import com.facucastro.focusguard.presentation.core.theme.FocusGuardTheme
import com.facucastro.focusguard.presentation.history.HistoryScreen
import com.facucastro.focusguard.presentation.home.view.HomeScreen
import com.facucastro.focusguard.presentation.login.view.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusGuardTheme {
                val isUserLoggedIn by mainViewModel.isUserLoggedIn
                    .collectAsStateWithLifecycle(initialValue = false)

                val backStack = rememberNavBackStack(AppRoute.Login)

                LaunchedEffect(isUserLoggedIn) {
                    if (isUserLoggedIn) {
                        if (backStack.contains(AppRoute.Login)) {
                            backStack.clear()
                            backStack.add(AppRoute.Main)
                        }
                    } else {
                        if (!backStack.contains(AppRoute.Login)) {
                            backStack.clear()
                            backStack.add(AppRoute.Login)
                        }
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<AppRoute.Login> {
                            LoginScreen(
                                onNavigateToHome = {
                                    backStack.clear()
                                    backStack.add(AppRoute.Main)
                                },
                            )
                        }
                        entry<AppRoute.Main> {
                            MainContent()
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun MainContent() {
        val snackbarHostState = remember { SnackbarHostState() }
        val tabBackStack = remember { TopLevelBackStack<MainTab>(MainTab.Home) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    val itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    )
                    NavigationBarItem(
                        selected = tabBackStack.topLevelKey == MainTab.Home,
                        onClick = { tabBackStack.switchTab(MainTab.Home) },
                        icon = { Icon(Icons.Filled.Timer, contentDescription = "Focus") },
                        label = { Text("FOCUS") },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = tabBackStack.topLevelKey == MainTab.History,
                        onClick = { tabBackStack.switchTab(MainTab.History) },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Statistics") },
                        label = { Text("STATISTICS") },
                        colors = itemColors,
                    )
                }
            },
        ) { innerPadding ->
            NavDisplay(
                backStack = tabBackStack.backStack,
                onBack = { tabBackStack.removeLast() },
                entryProvider = entryProvider {
                    entry<MainTab.Home> {
                        HomeScreen(
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                    entry<MainTab.History> {
                        HistoryScreen(modifier = Modifier.padding(innerPadding))
                    }
                },
            )
        }
    }
}
