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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facucastro.focusguard.data.service.FocusSessionService
import com.facucastro.focusguard.presentation.home.contract.HomeEffect
import com.facucastro.focusguard.presentation.home.contract.HomeIntent
import com.facucastro.focusguard.presentation.home.viewModel.HomeViewModel

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isNotificationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.POST_NOTIFICATIONS] == true
            } else {
                true
            }
        viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.RequestPermissions -> {
                    val permissionsToRequest = buildList {
                        add(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }.toTypedArray()
                    permissionLauncher.launch(permissionsToRequest)
                }
                HomeEffect.NotificationsPermissionDenied ->
                    snackbarHostState.showSnackbar("Notifications permission denied")
                HomeEffect.StartSessionService ->
                    context.startForegroundService(FocusSessionService.startIntent(context))
                HomeEffect.PauseSessionService ->
                    context.startService(FocusSessionService.pauseIntent(context))
                HomeEffect.ResumeSessionService ->
                    context.startService(FocusSessionService.resumeIntent(context))
                HomeEffect.StopSessionService ->
                    context.startService(FocusSessionService.stopIntent(context))
            }
        }
    }

    HomeContent(
        state = state,
        modifier = modifier,
        onStartClicked = { viewModel.handleIntent(HomeIntent.StartClicked) },
        onPauseClicked = { viewModel.handleIntent(HomeIntent.PauseClicked) },
        onResumeClicked = { viewModel.handleIntent(HomeIntent.ResumeClicked) },
        onStopClicked = { viewModel.handleIntent(HomeIntent.StopClicked) },
    )
}
