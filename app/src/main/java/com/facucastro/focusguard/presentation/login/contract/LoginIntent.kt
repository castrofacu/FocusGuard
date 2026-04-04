package com.facucastro.focusguard.presentation.login.contract

sealed interface LoginIntent {
    data object SignInWithGoogleClicked : LoginIntent
    data object SignInAnonymously : LoginIntent
}
