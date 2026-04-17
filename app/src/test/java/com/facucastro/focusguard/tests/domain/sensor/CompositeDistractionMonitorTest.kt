package com.facucastro.focusguard.tests.domain.sensor

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.sensor.CompositeDistractionMonitor
import com.facucastro.focusguard.providers.domain.sensor.FakeDistractionMonitor
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.utils.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompositeDistractionMonitorTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        io.mockk.unmockkStatic(android.util.Log::class)
    }

    private fun buildComposite(
        vararg monitors: FakeDistractionMonitor,
    ): CompositeDistractionMonitor = CompositeDistractionMonitor(monitors.toList(), testDispatcher)

    @Test
    fun `GIVEN single monitor WHEN it emits an event THEN composite forwards it`() =
        runTest(testDispatcher) {
        // GIVEN
        val monitor = providesFakeDistractionMonitor()
        val composite = buildComposite(monitor)
        val received = mutableListOf<DistractionEvent>()

        // Subscribe before any intents.
        val job = launch { composite.events.toList(received) }
        composite.start()

        // WHEN
        monitor.emit(DistractionEvent.Movement)
        advanceUntilIdle()

        // THEN
        job.cancel()
        assertTrue(received.contains(DistractionEvent.Movement))
    }

    @Test
    fun `GIVEN two monitors WHEN each emits an event THEN composite forwards both`() =
        runTest(testDispatcher) {
        // GIVEN
        val m1 = providesFakeDistractionMonitor()
        val m2 = providesFakeDistractionMonitor()
        val composite = buildComposite(m1, m2)
        val received = mutableListOf<DistractionEvent>()

        val job = launch { composite.events.toList(received) }
        composite.start()

        // WHEN
        m1.emit(DistractionEvent.Movement)
        m2.emit(DistractionEvent.Noise)
        advanceUntilIdle()

        // THEN
        job.cancel()
        assertEquals(2, received.size)
        assertTrue(received.contains(DistractionEvent.Movement))
        assertTrue(received.contains(DistractionEvent.Noise))
    }

    @Test
    fun `GIVEN two monitors WHEN one emits multiple events THEN all are forwarded`() =
        runTest(testDispatcher) {
        // GIVEN
        val m1 = providesFakeDistractionMonitor()
        val m2 = providesFakeDistractionMonitor()
        val composite = buildComposite(m1, m2)
        val received = mutableListOf<DistractionEvent>()

        val job = launch { composite.events.toList(received) }
        composite.start()

        // WHEN
        m1.emit(DistractionEvent.Movement)
        m1.emit(DistractionEvent.Noise)
        m1.emit(DistractionEvent.Movement)
        advanceUntilIdle()

        // THEN
        job.cancel()
        assertEquals(3, received.size)
    }

    @Test
    fun `GIVEN empty monitor list WHEN start and stop THEN no exception and events is silent`() =
        runTest(testDispatcher) {
            // GIVEN
            val composite = CompositeDistractionMonitor(emptyList())
            val received = mutableListOf<DistractionEvent>()

            val job = launch { composite.events.toList(received) }

            // WHEN
            composite.start()
            advanceUntilIdle()
            composite.stop()
            advanceUntilIdle()

            // THEN
            job.cancel()
            assertTrue(received.isEmpty())
        }

    @Test
    fun `GIVEN started composite WHEN stop THEN events emitted after stop are not forwarded`() =
        runTest(testDispatcher) {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val composite = buildComposite(monitor)
            val received = mutableListOf<DistractionEvent>()

            val job = launch { composite.events.toList(received) }
            composite.start()
            advanceUntilIdle()

            // WHEN — stop then emit
            composite.stop()
            advanceUntilIdle()
            monitor.emit(DistractionEvent.Noise)
            advanceUntilIdle()

            // THEN — nothing new forwarded after stop
            job.cancel()
            assertTrue(received.isEmpty())
        }

    @Test
    fun `GIVEN already started composite WHEN start called again THEN events are still forwarded correctly`() =
        runTest(testDispatcher) {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val composite = buildComposite(monitor)
            val received = mutableListOf<DistractionEvent>()

            val job = launch { composite.events.toList(received) }
            composite.start()
            advanceUntilIdle()

            // WHEN — second start (idempotency: previous scope cancelled, new one created)
            composite.start()
            advanceUntilIdle()

            monitor.emit(DistractionEvent.Movement)
            advanceUntilIdle()

            // THEN — event arrives despite double-start
            job.cancel()
            assertTrue(received.contains(DistractionEvent.Movement))
        }
}
