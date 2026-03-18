package com.facucastro.focusguard.presentation.login

class LoginContract {

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Error(val message: String) : State
    }

    sealed interface Intent {
        data object SignInWithGoogleClicked : Intent
        data class GoogleSignInResult(val idToken: String) : Intent
        data class GoogleSignInFailed(val message: String) : Intent
        data object SignInAnonymously : Intent
    }

    sealed interface Effect {
        data object NavigateToHome : Effect
        data object LaunchGoogleSignIn : Effect
    }
}
