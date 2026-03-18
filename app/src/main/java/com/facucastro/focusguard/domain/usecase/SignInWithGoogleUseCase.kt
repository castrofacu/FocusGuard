package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Unit> =
        authRepository.signInWithGoogle(idToken)
}
