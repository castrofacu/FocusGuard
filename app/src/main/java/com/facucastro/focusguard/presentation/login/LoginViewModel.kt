package com.facucastro.focusguard.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.usecase.GetGoogleIdTokenUseCase
import com.facucastro.focusguard.domain.usecase.SignInAnonymouslyUseCase
import com.facucastro.focusguard.domain.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getGoogleIdToken: GetGoogleIdTokenUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInAnonymously: SignInAnonymouslyUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow<LoginContract.State>(LoginContract.State.Idle)
    val viewState: StateFlow<LoginContract.State> = _viewState.asStateFlow()

    private val _effect = Channel<LoginContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: LoginContract.Intent) {
        when (intent) {
            is LoginContract.Intent.SignInWithGoogleClicked -> {
                performSignIn {
                    getGoogleIdToken(intent.context)
                        .mapCatching { token ->
                            signInWithGoogle(token).getOrThrow()
                        }
                }
            }
            LoginContract.Intent.SignInAnonymously -> {
                performSignIn { signInAnonymously() }
            }
        }
    }

    private fun performSignIn(signInBlock: suspend () -> Result<Unit>) {
        if (_viewState.value is LoginContract.State.Loading) return

        viewModelScope.launch {
            _viewState.value = LoginContract.State.Loading
            val result = signInBlock()

            if (result.isSuccess) {
                _viewState.value = LoginContract.State.Idle
                _effect.send(LoginContract.Effect.NavigateToHome)
            } else {
                val message = result.exceptionOrNull()?.message ?: "Unknown error"
                _viewState.value = LoginContract.State.Error(message)
            }
        }
    }
}
