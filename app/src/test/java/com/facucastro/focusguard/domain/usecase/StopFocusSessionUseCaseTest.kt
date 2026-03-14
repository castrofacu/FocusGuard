package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import com.facucastro.focusguard.domain.repository.mockFocusRepository
import com.facucastro.focusguard.domain.time.FakeTimeProvider
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopFocusSessionUseCaseTest {

    @Test
    fun `GIVEN valid time and repository saves WHEN invoke THEN returns success and session updated`() = runTest {
        // GIVEN
        val startTime = 1672531200000L
        val laterTime = startTime + 5000L // 5 seconds later
        val fakeTimeProvider = FakeTimeProvider(timeToReturn = laterTime)
        val repository = mockFocusRepository()
        val useCase = StopFocusSessionUseCase(repository, fakeTimeProvider)
        val session = FocusSession(id = startTime, startTime = startTime, durationSeconds = 0, distractionCount = 0)

        // WHEN
        val result = useCase(session, distractionCount = 2)

        // THEN
        assertTrue(result.isSuccess)
        val expectedDuration = ((laterTime - startTime) / 1000).toInt()
        val expected = session.copy(durationSeconds = expectedDuration, distractionCount = 2)
        coVerify { repository.saveSession(expected) }
    }

    @Test(expected = RuntimeException::class)
    fun `GIVEN time provider throws when invoke THEN propagates exception`() = runTest {
        // GIVEN
        val fakeTimeProvider = FakeTimeProvider(shouldThrow = true)
        val repository = mockk<FocusRepository>()
        val useCase = StopFocusSessionUseCase(repository, fakeTimeProvider)
        val session = FocusSession(id = 1L, startTime = 1L, durationSeconds = 0, distractionCount = 0)

        // WHEN / THEN (exception expected)
        useCase(session, 0)
    }

    @Test
    fun `GIVEN repository save fails WHEN invoke THEN returns failure result`() = runTest {
        // GIVEN
        val fakeTimeProvider = FakeTimeProvider(timeToReturn = 2000L)
        val expectedError = RuntimeException("DB Error")
        val repository = mockFocusRepository(saveResult = Result.failure(expectedError))
        val useCase = StopFocusSessionUseCase(repository, fakeTimeProvider)
        val session = FocusSession(id = 1000L, startTime = 1000L, durationSeconds = 0, distractionCount = 0)

        // WHEN
        val result = useCase(session, 0)

        // THEN
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }

}
