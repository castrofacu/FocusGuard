package com.facucastro.focusguard.presentation.home.state

sealed interface HomeEvent {
    data object RequestPermissions : HomeEvent
    data object NotificationsPermissionDenied : HomeEvent
    data object FailedToSaveSession : HomeEvent
}