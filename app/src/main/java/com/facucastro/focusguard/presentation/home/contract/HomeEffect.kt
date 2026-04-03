package com.facucastro.focusguard.presentation.home.contract

sealed interface HomeEffect {
    data object RequestPermissions : HomeEffect
    data object NotificationsPermissionDenied : HomeEffect
    data object FailedToSaveSession : HomeEffect
}
