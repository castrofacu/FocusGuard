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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facucastro.focusguard.presentation.history.HistoryScreen
import com.facucastro.focusguard.presentation.home.view.HomeScreen
import com.facucastro.focusguard.presentation.login.view.LoginScreen
import com.facucastro.focusguard.presentation.core.theme.FocusGuardTheme
import dagger.hilt.android.AndroidEntryPoint

private enum class Tab { Focus, Statistics }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusGuardTheme {
                val isUserLoggedIn by mainViewModel.isUserLoggedIn.collectAsStateWithLifecycle(initialValue = false)

                if (isUserLoggedIn) {
                    MainAppContent()
                } else {
                    LoginScreen()
                }
            }
        }
    }

    @Composable
    private fun MainAppContent() {
        var selectedTab by remember { mutableStateOf(Tab.Focus) }
        val snackbarHostState = remember { SnackbarHostState() }

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
                        selected = selectedTab == Tab.Focus,
                        onClick = { selectedTab = Tab.Focus },
                        icon = { Icon(Icons.Filled.Timer, contentDescription = "Focus") },
                        label = { Text("FOCUS") },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = selectedTab == Tab.Statistics,
                        onClick = { selectedTab = Tab.Statistics },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Statistics") },
                        label = { Text("STATISTICS") },
                        colors = itemColors,
                    )
                }
            },
        ) { innerPadding ->
            when (selectedTab) {
                Tab.Focus -> HomeScreen(
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.padding(innerPadding),
                )
                Tab.Statistics -> HistoryScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
