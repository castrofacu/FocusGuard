package com.facucastro.focusguard.presentation.home.contract

sealed interface HomeIntent {
    data object StartClicked : HomeIntent
    data class PermissionsResult(val isNotificationGranted: Boolean) : HomeIntent
    data object PauseClicked : HomeIntent
    data object ResumeClicked : HomeIntent
    data object StopClicked : HomeIntent
}
