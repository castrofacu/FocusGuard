package com.facucastro.focusguard.presentation.home.view

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facucastro.focusguard.presentation.home.state.HomeEvent
import com.facucastro.focusguard.presentation.home.viewModel.HomeViewModel

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isNotificationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.POST_NOTIFICATIONS] == true
            } else {
                true
            }
        viewModel.onPermissionsResult(isNotificationGranted)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.RequestPermissions -> {
                    val permissionsToRequest = buildList {
                        add(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }.toTypedArray()
                    permissionLauncher.launch(permissionsToRequest)
                }
                HomeEvent.FailedToSaveSession ->
                    snackbarHostState.showSnackbar("Failed to save session")
                HomeEvent.NotificationsPermissionDenied ->
                    snackbarHostState.showSnackbar("Notifications permission denied")
            }
        }
    }

    HomeContent(
        uiState = uiState,
        modifier = modifier,
        onStartClicked = viewModel::onStartClicked,
        onPauseClicked = viewModel::onPauseClicked,
        onResumeClicked = viewModel::onResumeClicked,
        onStopClicked = viewModel::onStopClicked,
    )
}