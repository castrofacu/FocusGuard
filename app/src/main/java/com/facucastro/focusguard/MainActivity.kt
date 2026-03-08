package com.facucastro.focusguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.facucastro.focusguard.presentation.history.HistoryScreen
import com.facucastro.focusguard.presentation.home.view.HomeScreen
import com.facucastro.focusguard.ui.theme.FocusGuardTheme
import dagger.hilt.android.AndroidEntryPoint

private enum class Tab { Timer, History }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusGuardTheme {
                var selectedTab by remember { mutableStateOf(Tab.Timer) }
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == Tab.Timer,
                                onClick = { selectedTab = Tab.Timer },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Timer,
                                        contentDescription = "Focus",
                                    )
                                },
                                label = { Text("Focus") },
                            )
                            NavigationBarItem(
                                selected = selectedTab == Tab.History,
                                onClick = { selectedTab = Tab.History },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = "History",
                                    )
                                },
                                label = { Text("History") },
                            )
                        }
                    },
                ) { innerPadding ->
                    when (selectedTab) {
                        Tab.Timer -> HomeScreen(
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier.padding(innerPadding),
                        )
                        Tab.History -> HistoryScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
