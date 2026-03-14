package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.mockFocusRepository
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetHistoryUseCaseTest {

    @Test
    fun `GIVEN repository with history WHEN invoke THEN returns repository flow`() = runTest {
        // GIVEN
        val session = FocusSession(id = 1L, startTime = 1L, durationSeconds = 60, distractionCount = 0)
        val repository = mockFocusRepository(historyFlow = flowOf(listOf(session)))
        val useCase = GetHistoryUseCase(repository)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(listOf(session), result)
        coVerify { repository.getHistory() }
    }

}
