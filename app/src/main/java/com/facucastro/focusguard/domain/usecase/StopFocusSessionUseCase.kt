package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.repository.FocusRepository
import javax.inject.Inject

class StopFocusSessionUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(id: Long, distractionCount: Int) =
        repository.stopSession(id, distractionCount)
}
