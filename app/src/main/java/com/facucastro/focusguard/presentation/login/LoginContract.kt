package com.facucastro.focusguard.presentation.login

class LoginContract {

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Error(val message: String) : State
    }

    sealed interface Intent {
        data class SignInWithGoogle(val idToken: String) : Intent
        data object SignInAnonymously : Intent
        data object ErrorDisplayed : Intent
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
        data class ShowError(val message: String) : Effect
    }
}
