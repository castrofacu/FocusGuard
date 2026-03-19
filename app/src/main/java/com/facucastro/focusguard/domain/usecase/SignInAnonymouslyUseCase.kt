package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.repository.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        authRepository.signInAnonymously()
}
