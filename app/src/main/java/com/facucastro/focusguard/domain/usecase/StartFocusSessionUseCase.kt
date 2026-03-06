package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import javax.inject.Inject

class StartFocusSessionUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(): FocusSession = repository.startSession()
}
