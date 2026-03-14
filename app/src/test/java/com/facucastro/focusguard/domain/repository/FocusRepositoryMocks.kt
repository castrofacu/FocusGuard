package com.facucastro.focusguard.domain.repository

import com.facucastro.focusguard.domain.model.FocusSession
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

fun mockFocusRepository(
    saveResult: Result<Unit> = Result.success(Unit),
    historyFlow: Flow<List<FocusSession>> = emptyFlow()
): FocusRepository {
    return mockk<FocusRepository> {
        coEvery { saveSession(any()) } returns saveResult
        coEvery { getHistory() } returns historyFlow
    }
}