package com.facucastro.focusguard.presentation.login.viewModel

import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.usecase.GetGoogleIdTokenUseCase
import com.facucastro.focusguard.domain.usecase.SignInAnonymouslyUseCase
import com.facucastro.focusguard.domain.usecase.SignInWithGoogleUseCase
import com.facucastro.focusguard.presentation.core.viewmodel.BaseMviViewModel
import com.facucastro.focusguard.presentation.login.contract.LoginEffect
import com.facucastro.focusguard.presentation.login.contract.LoginIntent
import com.facucastro.focusguard.presentation.login.contract.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getGoogleIdToken: GetGoogleIdTokenUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInAnonymously: SignInAnonymouslyUseCase
) : BaseMviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState.Idle) {

    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.SignInWithGoogleClicked -> {
                performSignIn {
                    getGoogleIdToken()
                        .mapCatching { token ->
                            signInWithGoogle(token).getOrThrow()
                        }
                }
            }
            LoginIntent.SignInAnonymously -> {
                performSignIn { signInAnonymously() }
            }
        }
    }

    private fun performSignIn(signInBlock: suspend () -> Result<Unit>) {
        if (state.value is LoginState.Loading) return

        setState { LoginState.Loading }
        viewModelScope.launch {
            val result = signInBlock()

            if (result.isSuccess) {
                setState { LoginState.Idle }
                sendEffect(LoginEffect.NavigateToHome)
            } else {
                val message = result.exceptionOrNull()?.message ?: "Unknown error"
                setState { LoginState.Error(message) }
            }
        }
    }
}
