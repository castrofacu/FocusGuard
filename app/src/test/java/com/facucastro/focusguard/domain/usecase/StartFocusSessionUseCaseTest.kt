package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.time.FakeTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class StartFocusSessionUseCaseTest {

    @Test
    fun `GIVEN valid time when invoke THEN returns focus session with correct timestamps`() {
        // GIVEN
        val fixedTime = 1672531200000L
        val fakeTimeProvider = FakeTimeProvider(timeToReturn = fixedTime)
        val useCase = StartFocusSessionUseCase(fakeTimeProvider)

        // WHEN
        val session = useCase()

        // THEN
        assertEquals(fixedTime, session.id)
        assertEquals(fixedTime, session.startTime)
        assertEquals(0, session.durationSeconds)
        assertEquals(0, session.distractionCount)
    }

    @Test(expected = RuntimeException::class)
    fun `GIVEN time provider throws when invoke THEN propagates exception`() {
        // GIVEN
        val fakeTimeProvider = FakeTimeProvider(shouldThrow = true)
        val useCase = StartFocusSessionUseCase(fakeTimeProvider)

        // WHEN / THEN (exception expected)
        useCase()
    }

}
