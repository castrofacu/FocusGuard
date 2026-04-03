package com.facucastro.focusguard.presentation.login.contract

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
}
