package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.time.TimeProvider
import com.facucastro.focusguard.domain.usecase.FocusTimerUseCase
import com.facucastro.focusguard.providers.domain.time.StepTimeProvider
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
        val useCase = FocusTimerUseCase(fakeTime)
        val results = mutableListOf<Int>()

        // WHEN
        val job = launch {
            useCase(startTimeMillis = 0L).take(1).toList(results)
        }
        fakeTime.now = 1_000L
        advanceTimeBy(600L) // past the 500ms poll delay

        // THEN
        job.join()
        assertEquals(listOf(1), results)
    }

    @Test
    fun `GIVEN startTime of 1000 WHEN 3 seconds have elapsed THEN emits 3`() = runTest {
        // GIVEN
        val startTime = 1_000L
        val fakeTime = StepTimeProvider(initialMillis = startTime)
        val useCase = FocusTimerUseCase(fakeTime)
        val results = mutableListOf<Int>()

        // WHEN
        val job = launch {
            useCase(startTimeMillis = startTime).take(1).toList(results)
        }
        fakeTime.now = startTime + 3_000L
        advanceTimeBy(600L)

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
            val useCase = FocusTimerUseCase(fakeTime)
            val results = mutableListOf<Int>()

            // WHEN — advance time in 3 steps, each 500ms (the poll interval)
            val job = launch {
                useCase(startTimeMillis = 0L).take(3).toList(results)
            }
            repeat(3) {
                tick++
                advanceTimeBy(600L)
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
            val useCase = FocusTimerUseCase(fakeTime)
            val results = mutableListOf<Int>()

            // WHEN
            val job = launch {
                useCase(startTimeMillis = 0L).take(1).toList(results)
            }
            advanceTimeBy(600L)

            // THEN
            job.join()
            assertEquals(listOf(1), results)
        }
}
