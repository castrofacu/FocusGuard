package com.facucastro.focusguard.tests.domain.timer

import com.facucastro.focusguard.providers.domain.time.StepTimeProvider
import com.facucastro.focusguard.providers.domain.timer.providesFocusSessionTimer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSessionTimerTest {

    @Test
    fun `GIVEN timer at t=0 WHEN 5s elapsed THEN elapsedSeconds is 5`() {
        // GIVEN
        val fakeTime = StepTimeProvider(initialMillis = 0L)
        val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)

        // WHEN
        fakeTime.now = 5_000L

        // THEN
        assertEquals(5, timer.elapsedSeconds())
    }

    @Test
    fun `GIVEN timer running WHEN paused and 10s pass THEN elapsedSeconds does not increase`() {
        // GIVEN
        val fakeTime = StepTimeProvider(initialMillis = 0L)
        val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
        fakeTime.now = 2_000L
        val elapsedBeforePause = timer.elapsedSeconds()

        // WHEN
        timer.pause()
        fakeTime.now = 12_000L

        // THEN
        assertEquals(elapsedBeforePause, timer.elapsedSeconds())
        assertTrue(timer.isPaused)
    }

    @Test
    fun `GIVEN timer paused WHEN resumed after 10s THEN elapsedSeconds excludes paused duration`() {
        // GIVEN
        val fakeTime = StepTimeProvider(initialMillis = 0L)
        val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)

        // Run 2s, then pause for 10s, then run 1s more
        fakeTime.now = 2_000L
        timer.pause()
        fakeTime.now = 12_000L

        // WHEN
        timer.resume()
        fakeTime.now = 13_000L

        // THEN — active time = 2s + 1s = 3, paused 10s must be excluded
        assertEquals(3, timer.elapsedSeconds())
        assertFalse(timer.isPaused)
    }

    @Test
    fun `GIVEN timer not paused WHEN resume called THEN is a no-op`() {
        // GIVEN
        val fakeTime = StepTimeProvider(initialMillis = 0L)
        val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
        fakeTime.now = 3_000L

        // WHEN — resume without a prior pause
        timer.resume()
        fakeTime.now = 5_000L

        // THEN — elapsed is 5s (no offset applied)
        assertEquals(5, timer.elapsedSeconds())
        assertFalse(timer.isPaused)
    }

    @Test
    fun `GIVEN timer already paused WHEN pause called again THEN second pause is a no-op`() {
        // GIVEN
        val fakeTime = StepTimeProvider(initialMillis = 0L)
        val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
        fakeTime.now = 2_000L
        timer.pause()

        // WHEN — advance time and call pause again
        fakeTime.now = 5_000L
        timer.pause()

        // THEN — timer still paused; elapsed is still 2s (frozen at first pause snapshot)
        assertTrue(timer.isPaused)
        fakeTime.now = 10_000L
        assertEquals(2, timer.elapsedSeconds())
    }
}
