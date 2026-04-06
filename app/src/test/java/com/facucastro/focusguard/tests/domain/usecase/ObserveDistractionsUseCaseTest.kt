package com.facucastro.focusguard.tests.domain.usecase

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.usecase.ObserveDistractionsUseCase
import com.facucastro.focusguard.providers.domain.notification.providesNotifierMock
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.providers.domain.time.StepTimeProvider
import com.facucastro.focusguard.providers.domain.time.providesFakeTimeProvider
import com.facucastro.focusguard.utils.MainDispatcherRule
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveDistractionsUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun `GIVEN monitor emits an event WHEN collecting THEN event is forwarded to the flow`() =
        runTest {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val useCase = ObserveDistractionsUseCase(
                distractionMonitor = monitor,
                distractionNotifier = providesNotifierMock(),
                timeProvider = providesFakeTimeProvider(),
            )
            val results = mutableListOf<DistractionEvent>()

            // Subscribe before emitting so the collector is established.
            val job = launch { useCase().toList(results) }
            runCurrent()

            // WHEN
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            job.cancel()
            assertEquals(listOf(DistractionEvent.Movement), results)
        }

    @Test
    fun `GIVEN monitor emits multiple events WHEN collecting THEN all events are forwarded`() =
        runTest {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val useCase = ObserveDistractionsUseCase(
                distractionMonitor = monitor,
                distractionNotifier = providesNotifierMock(),
                timeProvider = providesFakeTimeProvider(),
            )
            val results = mutableListOf<DistractionEvent>()

            // Subscribe before emitting so the collector is established.
            val job = launch { useCase().toList(results) }
            runCurrent()

            // WHEN
            monitor.emit(DistractionEvent.Movement)
            monitor.emit(DistractionEvent.Noise)
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            job.cancel()
            assertEquals(
                listOf(DistractionEvent.Movement, DistractionEvent.Noise, DistractionEvent.Movement),
                results,
            )
        }

    @Test
    fun `GIVEN first event WHEN notifier is called THEN notifier receives the event`() = runTest {
        // GIVEN
        val notifier = providesNotifierMock()
        val monitor = providesFakeDistractionMonitor()
        val useCase = ObserveDistractionsUseCase(
            distractionMonitor = monitor,
            distractionNotifier = notifier,
            timeProvider = providesFakeTimeProvider(timeToReturn = 0L),
        )

        // Subscribe before emitting so the collector is established.
        val job = launch { useCase().toList() }
        runCurrent()

        // WHEN
        monitor.emit(DistractionEvent.Noise)
        runCurrent()

        // THEN
        job.cancel()
        verify(exactly = 1) { notifier.notifyDistraction(DistractionEvent.Noise) }
    }

    @Test
    fun `GIVEN two events within throttle window WHEN collecting THEN notifier is called only once`() =
        runTest {
            // GIVEN — both events arrive at the same timestamp (within 2s throttle window)
            val notifier = providesNotifierMock()
            val monitor = providesFakeDistractionMonitor()
            val useCase = ObserveDistractionsUseCase(
                distractionMonitor = monitor,
                distractionNotifier = notifier,
                timeProvider = providesFakeTimeProvider(timeToReturn = 1_000L),
            )

            // Subscribe before emitting so the collector is established.
            val job = launch { useCase().toList() }
            runCurrent()

            // WHEN
            monitor.emit(DistractionEvent.Movement)
            monitor.emit(DistractionEvent.Noise)
            runCurrent()

            // THEN
            job.cancel()
            verify(exactly = 1) { notifier.notifyDistraction(any()) }
        }

    @Test
    fun `GIVEN two events outside throttle window WHEN collecting THEN notifier is called twice`() =
        runTest {
            // GIVEN
            val notifier = providesNotifierMock()
            val monitor = providesFakeDistractionMonitor()
            val time = StepTimeProvider()
            val useCase = ObserveDistractionsUseCase(
                distractionMonitor = monitor,
                distractionNotifier = notifier,
                timeProvider = time,
            )

            // Subscribe before emitting so the collector is established.
            val job = launch { useCase().toList() }
            runCurrent()

            // WHEN — first event at t=0, allowed through
            time.now = 0L
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // WHEN — second event at t=3000, outside the 2s window, also allowed through
            time.now = 3_000L
            monitor.emit(DistractionEvent.Noise)
            runCurrent()

            // THEN
            job.cancel()
            verify(exactly = 2) { notifier.notifyDistraction(any()) }
        }

    @Test
    fun `GIVEN event at t=0 and event at t=1999 WHEN collecting THEN notifier is called only once`() =
        runTest {
            // GIVEN
            val notifier = providesNotifierMock()
            val monitor = providesFakeDistractionMonitor()
            val time = StepTimeProvider()
            val useCase = ObserveDistractionsUseCase(
                distractionMonitor = monitor,
                distractionNotifier = notifier,
                timeProvider = time,
            )

            // Subscribe before emitting so the collector is established.
            val job = launch { useCase().toList() }
            runCurrent()

            // WHEN — first event at t=0, allowed through
            time.now = 0L
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // WHEN — second event at t=1999, still inside the 2s window, throttled
            time.now = 1_999L
            monitor.emit(DistractionEvent.Noise)
            runCurrent()

            // THEN
            job.cancel()
            verify(exactly = 1) { notifier.notifyDistraction(any()) }
        }

    @Test
    fun `GIVEN event at t=0 and event at t=2000 WHEN collecting THEN notifier is called twice`() =
        runTest {
            // GIVEN
            val notifier = providesNotifierMock()
            val monitor = providesFakeDistractionMonitor()
            val time = StepTimeProvider()
            val useCase = ObserveDistractionsUseCase(
                distractionMonitor = monitor,
                distractionNotifier = notifier,
                timeProvider = time,
            )

            // Subscribe before emitting so the collector is established.
            val job = launch { useCase().toList() }
            runCurrent()

            // WHEN — first event at t=0, allowed through
            time.now = 0L
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // WHEN — second event at t=2000, exactly at boundary, allowed through
            time.now = 2_000L
            monitor.emit(DistractionEvent.Noise)
            runCurrent()

            // THEN
            job.cancel()
            verify(exactly = 2) { notifier.notifyDistraction(any()) }
        }
}
