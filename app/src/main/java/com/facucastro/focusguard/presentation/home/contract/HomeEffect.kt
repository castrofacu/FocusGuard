package com.facucastro.focusguard.presentation.home.contract

sealed interface HomeEffect {
    data object RequestPermissions : HomeEffect
    data object NotificationsPermissionDenied : HomeEffect
    data object FailedToSaveSession : HomeEffect

    data object StartSessionService : HomeEffect
    data object PauseSessionService : HomeEffect
    data object ResumeSessionService : HomeEffect
    data object StopSessionService : HomeEffect
}
