package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLoginStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = authRepository.isUserLoggedIn
}
