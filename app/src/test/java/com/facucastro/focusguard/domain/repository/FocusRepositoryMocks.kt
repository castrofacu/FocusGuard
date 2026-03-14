package com.facucastro.focusguard.domain.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow

fun mockFocusRepository(
    saveResult: Result<Unit> = Result.success(Unit)
): FocusRepository {
    return mockk<FocusRepository> {
        coEvery { saveSession(any()) } returns saveResult
        coEvery { getHistory() } returns emptyFlow()
    }
}