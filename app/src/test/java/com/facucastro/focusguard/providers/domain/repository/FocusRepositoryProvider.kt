package com.facucastro.focusguard.providers.domain.repository

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

fun providesMockFocusRepository(
    saveResult: Result<Unit> = Result.success(Unit),
    historyFlow: Flow<List<FocusSession>> = emptyFlow()
): FocusRepository {
    return mockk<FocusRepository> {
        coEvery { saveSession(any()) } returns saveResult
        every { getHistory() } returns historyFlow
    }
}