package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.mockFocusRepository
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
        verify { repository.getHistory() }
    }

    @Test
    fun `GIVEN empty repository WHEN invoke THEN returns empty list`() = runTest {
        // GIVEN
        val repository = mockFocusRepository(historyFlow = flowOf(emptyList()))
        val useCase = GetHistoryUseCase(repository)

        // WHEN
        val result = useCase().first()

        // THEN
        assertEquals(emptyList<FocusSession>(), result)
        verify { repository.getHistory() }
    }

    @Test(expected = RuntimeException::class)
    fun `GIVEN repository throws exception WHEN invoke THEN exception is propagated`() = runTest {
        // GIVEN
        val exceptionFlow = flow<List<FocusSession>> { throw RuntimeException("Database error") }
        val repository = mockFocusRepository(historyFlow = exceptionFlow)
        val useCase = GetHistoryUseCase(repository)

        // WHEN // THEN
        useCase().first()
    }
}
