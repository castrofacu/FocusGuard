package com.facucastro.focusguard.providers.presentation.login.viewModel

import com.facucastro.focusguard.domain.usecase.GetGoogleIdTokenUseCase
import com.facucastro.focusguard.domain.usecase.SignInAnonymouslyUseCase
import com.facucastro.focusguard.domain.usecase.SignInWithGoogleUseCase
import com.facucastro.focusguard.presentation.login.viewModel.LoginViewModel
import com.facucastro.focusguard.providers.domain.auth.providesMockGoogleCredentialProvider
import com.facucastro.focusguard.providers.domain.repository.providesMockAuthRepository

fun providesLoginViewModel(
    idTokenResult: Result<String> = Result.success("mock_id_token"),
    signInWithGoogleResult: Result<Unit> = Result.success(Unit),
    signInAnonymouslyResult: Result<Unit> = Result.success(Unit)
): LoginViewModel {
    val googleCredentialProvider = providesMockGoogleCredentialProvider(idTokenResult = idTokenResult)
    val authRepository = providesMockAuthRepository(
        signInWithGoogleResult = signInWithGoogleResult,
        signInAnonymouslyResult = signInAnonymouslyResult
    )

    return LoginViewModel(
        getGoogleIdToken = GetGoogleIdTokenUseCase(googleCredentialProvider),
        signInWithGoogle = SignInWithGoogleUseCase(authRepository),
        signInAnonymously = SignInAnonymouslyUseCase(authRepository)
    )
}
