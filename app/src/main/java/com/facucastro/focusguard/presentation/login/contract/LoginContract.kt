package com.facucastro.focusguard.presentation.login.contract

import android.content.Context

class LoginContract {

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Error(val message: String) : State
    }

    sealed interface Intent {
        data class SignInWithGoogleClicked(val context: Context) : Intent
        data object SignInAnonymously : Intent
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
    }
}