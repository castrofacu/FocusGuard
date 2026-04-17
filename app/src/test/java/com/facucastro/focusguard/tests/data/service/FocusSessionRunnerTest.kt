package com.facucastro.focusguard.tests.data.service

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.session.FocusSessionState
import com.facucastro.focusguard.providers.data.service.providesFocusSessionRunner
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.utils.MainDispatcherRule
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FocusSessionRunnerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.i(any(), any()) } returns 0
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        io.mockk.unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `GIVEN new runner WHEN inspecting state THEN status is Idle`() = runTest {
        // GIVEN / WHEN
        val runner = providesFocusSessionRunner()

        // THEN
        assertEquals(SessionStatus.Idle, runner.state.value.status)
    }

    @Test
    fun `GIVEN new runner WHEN inspecting state THEN elapsed seconds is 0`() = runTest {
        // GIVEN / WHEN
        val runner = providesFocusSessionRunner()

        // THEN
        assertEquals(0, runner.state.value.elapsedSeconds)
    }

    @Test
    fun `GIVEN new runner WHEN inspecting state THEN distractionCount is 0`() = runTest {
        // GIVEN / WHEN
        val runner = providesFocusSessionRunner()

        // THEN
        assertEquals(0, runner.state.value.distractionCount)
    }

    @Test
    fun `GIVEN Idle state WHEN start THEN status transitions to Running`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()

        // WHEN
        runner.start()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Running, runner.state.value.status)
    }

    @Test
    fun `GIVEN Idle state WHEN start THEN distractionCount resets to 0`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()

        // WHEN
        runner.start()
        runCurrent()

        // THEN
        assertEquals(0, runner.state.value.distractionCount)
    }

    @Test
    fun `GIVEN Running state WHEN start called again THEN status remains Running`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()
        runner.start()
        runCurrent()

        // WHEN
        runner.start()
        runCurrent()

        // THEN — guard fires, no second session started, state stays Running
        assertEquals(SessionStatus.Running, runner.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN start called again THEN distractionCount is not reset`() =
        runTest {
            // GIVEN — start a session and inject one distraction
            val monitor = providesFakeDistractionMonitor()
            val runner = providesFocusSessionRunner(monitor = monitor)
            runner.start()
            runCurrent()
            monitor.emit(DistractionEvent.Noise)
            runCurrent()
            assertEquals(1, runner.state.value.distractionCount)

            // WHEN — spurious second start
            runner.start()
            runCurrent()

            // THEN — guard fires, count unchanged
            assertEquals(1, runner.state.value.distractionCount)
        }

    @Test
    fun `GIVEN Running state WHEN pause THEN status transitions to Paused`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()
        runner.start()
        runCurrent()

        // WHEN
        runner.pause()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Paused, runner.state.value.status)
    }

    @Test
    fun `GIVEN Idle state WHEN pause THEN status remains Idle`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()

        // WHEN
        runner.pause()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Idle, runner.state.value.status)
    }

    @Test
    fun `GIVEN Paused state WHEN pause again THEN status remains Paused`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()
        runner.start()
        runCurrent()
        runner.pause()
        runCurrent()

        // WHEN
        runner.pause()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Paused, runner.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN pause THEN lastDistractionEvent is cleared`() = runTest {
        // GIVEN — start and trigger a distraction
        val monitor = providesFakeDistractionMonitor()
        val runner = providesFocusSessionRunner(monitor = monitor)
        runner.start()
        runCurrent()
        monitor.emit(DistractionEvent.Movement)
        runCurrent()

        // WHEN
        runner.pause()
        runCurrent()

        // THEN
        assertNull(runner.state.value.lastDistractionEvent)
    }

    @Test
    fun `GIVEN Paused state WHEN resume THEN status transitions to Running`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()
        runner.start()
        runCurrent()
        runner.pause()
        runCurrent()

        // WHEN
        runner.resume()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Running, runner.state.value.status)
    }

    @Test
    fun `GIVEN Idle state WHEN resume THEN status remains Idle`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()

        // WHEN
        runner.resume()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Idle, runner.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN resume THEN status remains Running`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()
        runner.start()
        runCurrent()

        // WHEN
        runner.resume()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Running, runner.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN stop THEN status transitions to Idle`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()
        runner.start()
        runCurrent()

        // WHEN
        runner.stop()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Idle, runner.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN stop THEN elapsedSeconds resets to 0`() = runTest {
        // GIVEN — use a timer flow that emits one tick
        val timerFlow = MutableSharedFlow<Int>(extraBufferCapacity = 1)
        val runner = providesFocusSessionRunner(timerFlow = timerFlow)
        runner.start()
        runCurrent()
        timerFlow.emit(5)
        runCurrent()
        assertEquals(5, runner.state.value.elapsedSeconds)

        // WHEN
        runner.stop()
        runCurrent()

        // THEN
        assertEquals(0, runner.state.value.elapsedSeconds)
    }

    @Test
    fun `GIVEN Running state WHEN stop THEN distractionCount resets to 0`() = runTest {
        // GIVEN
        val monitor = providesFakeDistractionMonitor()
        val runner = providesFocusSessionRunner(monitor = monitor)
        runner.start()
        runCurrent()
        monitor.emit(DistractionEvent.Noise)
        runCurrent()
        assertEquals(1, runner.state.value.distractionCount)

        // WHEN
        runner.stop()
        runCurrent()

        // THEN
        assertEquals(0, runner.state.value.distractionCount)
    }

    @Test
    fun `GIVEN Idle state WHEN stop called without start THEN status remains Idle`() = runTest {
        // GIVEN
        val runner = providesFocusSessionRunner()

        // WHEN
        runner.stop()
        runCurrent()

        // THEN
        assertEquals(SessionStatus.Idle, runner.state.value.status)
    }

    @Test
    fun `GIVEN Running session WHEN timer emits elapsed THEN state reflects elapsed seconds`() =
        runTest {
            // GIVEN
            val timerFlow = MutableSharedFlow<Int>(extraBufferCapacity = 4)
            val runner = providesFocusSessionRunner(timerFlow = timerFlow)

            // Subscribe before any intents to capture all emissions.
            val states = mutableListOf<FocusSessionState>()
            val job = launch { runner.state.toList(states) }

            runner.start()
            runCurrent()

            // WHEN
            timerFlow.emit(1)
            timerFlow.emit(2)
            timerFlow.emit(3)
            runCurrent()

            // THEN
            job.cancel()
            assertEquals(3, runner.state.value.elapsedSeconds)
        }

    @Test
    fun `GIVEN Running session WHEN monitor emits event THEN distractionCount increments`() =
        runTest {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val runner = providesFocusSessionRunner(monitor = monitor)
            runner.start()
            runCurrent()

            // WHEN
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            assertEquals(1, runner.state.value.distractionCount)
        }

    @Test
    fun `GIVEN Running session WHEN monitor emits multiple events THEN distractionCount equals event count`() =
        runTest {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val runner = providesFocusSessionRunner(monitor = monitor)
            runner.start()
            runCurrent()

            // WHEN
            monitor.emit(DistractionEvent.Movement)
            monitor.emit(DistractionEvent.Noise)
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            assertEquals(3, runner.state.value.distractionCount)
        }

    @Test
    fun `GIVEN Running session WHEN monitor emits event THEN lastDistractionEvent is updated`() =
        runTest {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val runner = providesFocusSessionRunner(monitor = monitor)
            runner.start()
            runCurrent()

            // WHEN
            monitor.emit(DistractionEvent.Noise)
            runCurrent()

            // THEN
            assertEquals(DistractionEvent.Noise, runner.state.value.lastDistractionEvent)
        }

    @Test
    fun `GIVEN Running then Paused session WHEN monitor emits event while paused THEN distractionCount does not increment`() =
        runTest {
            // GIVEN
            val monitor = providesFakeDistractionMonitor()
            val runner = providesFocusSessionRunner(monitor = monitor)
            runner.start()
            runCurrent()
            runner.pause()
            runCurrent()

            // WHEN — emit while paused (monitor job was cancelled on pause)
            monitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            assertEquals(0, runner.state.value.distractionCount)
        }
}
