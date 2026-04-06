package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.time.TimeProvider
import com.facucastro.focusguard.domain.timer.FocusSessionTimer
import com.facucastro.focusguard.domain.usecase.FocusTimerUseCase
import com.facucastro.focusguard.providers.domain.time.StepTimeProvider
import com.facucastro.focusguard.providers.domain.timer.providesFocusSessionTimer
import com.facucastro.focusguard.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class FocusTimerUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `GIVEN startTime of 0 WHEN 1 second has elapsed THEN emits 1`() = runTest {
        // GIVEN
        val fakeTime = StepTimeProvider(initialMillis = 0L)
        val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
        val useCase = FocusTimerUseCase()
        val results = mutableListOf<Int>()

        // WHEN
        val job = launch {
            useCase(timer).take(1).toList(results)
        }
        fakeTime.now = 1_000L
        advanceTimeBy(1_101L)

        // THEN
        job.join()
        assertEquals(listOf(1), results)
    }

    @Test
    fun `GIVEN startTime of 1000 WHEN 3 seconds have elapsed THEN emits 3`() = runTest {
        // GIVEN
        val startTime = 1_000L
        val fakeTime = StepTimeProvider(initialMillis = startTime)
        val timer = providesFocusSessionTimer(startTimeMillis = startTime, timeProvider = fakeTime)
        val useCase = FocusTimerUseCase()
        val results = mutableListOf<Int>()

        // WHEN
        val job = launch {
            useCase(timer).take(1).toList(results)
        }
        fakeTime.now = startTime + 3_000L
        advanceTimeBy(1_001L)

        // THEN
        job.join()
        assertEquals(listOf(3), results)
    }

    @Test
    fun `GIVEN timer running WHEN multiple ticks pass THEN emits increasing elapsed seconds`() =
        runTest {
            // GIVEN
            var tick = 0
            val fakeTime = object : TimeProvider {
                override fun getCurrentTimeMillis(): Long = tick * 1_000L
                override fun getZoneId(): ZoneId = ZoneId.of("UTC")
            }
            val timer = FocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
            val useCase = FocusTimerUseCase()
            val results = mutableListOf<Int>()

            // WHEN — advance time in 3 steps, each 1000ms (the poll interval)
            val job = launch {
                useCase(timer).take(3).toList(results)
            }
            repeat(3) {
                tick++
                advanceTimeBy(1_001L)
            }

            // THEN
            job.join()
            assertEquals(listOf(1, 2, 3), results)
        }

    @Test
    fun `GIVEN elapsed time is fractional WHEN emitting THEN truncates to whole seconds`() =
        runTest {
            // GIVEN
            val fakeTime = StepTimeProvider(initialMillis = 1_700L)
            val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
            val useCase = FocusTimerUseCase()
            val results = mutableListOf<Int>()

            // WHEN
            val job = launch {
                useCase(timer).take(1).toList(results)
            }
            advanceTimeBy(1_001L)

            // THEN
            job.join()
            assertEquals(listOf(1), results)
        }

    @Test
    fun `GIVEN timer running WHEN paused THEN no new values are emitted while paused`() =
        runTest {
            // GIVEN
            val fakeTime = StepTimeProvider(initialMillis = 0L)
            val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
            val useCase = FocusTimerUseCase()
            val results = mutableListOf<Int>()

            val job = launch {
                useCase(timer).toList(results)
            }

            // Advance 1s — should emit 1
            fakeTime.now = 1_000L
            advanceTimeBy(1_001L)

            // WHEN — pause and advance 5 more seconds
            timer.pause()
            fakeTime.now = 6_000L
            advanceTimeBy(5_001L)

            // THEN — still only emitted the value from before the pause
            assertEquals(listOf(1), results)
            job.cancel()
        }

    @Test
    fun `GIVEN timer paused WHEN resumed THEN elapsed does not include paused duration`() =
        runTest {
            // GIVEN
            val fakeTime = StepTimeProvider(initialMillis = 0L)
            val timer = providesFocusSessionTimer(startTimeMillis = 0L, timeProvider = fakeTime)
            val useCase = FocusTimerUseCase()
            val results = mutableListOf<Int>()

            val job = launch {
                useCase(timer).toList(results)
            }

            // Run 2 seconds, then pause
            fakeTime.now = 2_000L
            advanceTimeBy(2_001L)
            timer.pause()

            // Wall-clock advances 10s while paused — elapsed must NOT increase
            fakeTime.now = 12_000L
            advanceTimeBy(10_001L)

            // WHEN — resume; wall-clock continues from 12s
            timer.resume()
            fakeTime.now = 13_000L
            advanceTimeBy(1_001L)

            // THEN — elapsed should be 3 (2s before pause + 1s after resume), not 13
            assertEquals(3, results.last())
            job.cancel()
        }
}
