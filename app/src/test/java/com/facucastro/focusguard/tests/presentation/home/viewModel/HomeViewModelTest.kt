package com.facucastro.focusguard.tests.presentation.home.viewModel

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.presentation.home.contract.HomeEffect
import com.facucastro.focusguard.presentation.home.contract.HomeIntent
import com.facucastro.focusguard.presentation.home.contract.HomeState
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.providers.presentation.home.viewModel.providesHomeViewModel
import com.facucastro.focusguard.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun `GIVEN viewModel WHEN initialized THEN state is default HomeState`() = runTest {
        // GIVEN / WHEN
        val viewModel = providesHomeViewModel()

        // THEN
        Assert.assertEquals(HomeState(), viewModel.state.value)
    }

    @Test
    fun `GIVEN viewModel WHEN initialized THEN status is Idle`() = runTest {
        // GIVEN / WHEN
        val viewModel = providesHomeViewModel()

        // THEN
        Assert.assertEquals(SessionStatus.Idle, viewModel.state.value.status)
    }

    @Test
    fun `GIVEN Idle state WHEN StartClicked intent THEN RequestPermissions effect is sent`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()
            val effects = mutableListOf<HomeEffect>()
            val job = launch { viewModel.effects.toList(effects) }

            // WHEN
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            // THEN
            Assert.assertTrue(effects.contains(HomeEffect.RequestPermissions))
            job.cancel()
        }

    @Test
    fun `GIVEN Running state WHEN StartClicked intent THEN no additional effect is sent`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()

            // Subscribe before any intents so the channel buffer is fully drained.
            val effects = mutableListOf<HomeEffect>()
            val job = launch { viewModel.effects.toList(effects) }

            // Bring the session to Running state.
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // Snapshot the effect count after setup — we expect exactly one RequestPermissions
            // from the first StartClicked above.
            val effectCountAfterSetup = effects.count { it is HomeEffect.RequestPermissions }

            // WHEN — a second StartClicked on an already-Running session
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            // THEN — no new RequestPermissions effect was emitted
            Assert.assertEquals(
                effectCountAfterSetup,
                effects.count { it is HomeEffect.RequestPermissions }
            )
            job.cancel()
        }

    @Test
    fun `GIVEN StartClicked WHEN PermissionsResult granted THEN state becomes Running`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            // WHEN
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // THEN
            Assert.assertEquals(SessionStatus.Running, viewModel.state.value.status)
        }

    @Test
    fun `GIVEN StartClicked WHEN PermissionsResult denied THEN NotificationsPermissionDenied effect is sent AND session starts`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            val effects = mutableListOf<HomeEffect>()
            val job = launch { viewModel.effects.toList(effects) }

            // WHEN
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = false))
            runCurrent()

            // THEN
            Assert.assertTrue(effects.contains(HomeEffect.NotificationsPermissionDenied))
            Assert.assertEquals(SessionStatus.Running, viewModel.state.value.status)
            job.cancel()
        }

    @Test
    fun `GIVEN session starts WHEN PermissionsResult THEN elapsedSeconds and distractionCount are reset to zero`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            // WHEN
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // THEN
            Assert.assertEquals(0, viewModel.state.value.elapsedSeconds)
            Assert.assertEquals(0, viewModel.state.value.distractionCount)
        }

    @Test
    fun `GIVEN Running state WHEN PauseClicked intent THEN state becomes Paused`() = runTest {
        // GIVEN
        val viewModel = providesHomeViewModel()
        viewModel.handleIntent(HomeIntent.StartClicked)
        viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
        runCurrent()

        // WHEN
        viewModel.handleIntent(HomeIntent.PauseClicked)
        runCurrent()

        // THEN
        Assert.assertEquals(SessionStatus.Paused, viewModel.state.value.status)
    }

    @Test
    fun `GIVEN Running state with lastDistractionEvent WHEN PauseClicked THEN lastDistractionEvent is cleared`() =
        runTest {
            // GIVEN
            val fakeMonitor = providesFakeDistractionMonitor()
            val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()
            fakeMonitor.emit(DistractionEvent.Movement)
            runCurrent()
            Assert.assertNotNull(viewModel.state.value.lastDistractionEvent)

            // WHEN
            viewModel.handleIntent(HomeIntent.PauseClicked)
            runCurrent()

            // THEN
            Assert.assertNull(viewModel.state.value.lastDistractionEvent)
        }

    @Test
    fun `GIVEN Paused state WHEN ResumeClicked intent THEN state becomes Running`() = runTest {
        // GIVEN
        val viewModel = providesHomeViewModel()
        viewModel.handleIntent(HomeIntent.StartClicked)
        viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
        viewModel.handleIntent(HomeIntent.PauseClicked)
        runCurrent()

        // WHEN
        viewModel.handleIntent(HomeIntent.ResumeClicked)
        runCurrent()

        // THEN
        Assert.assertEquals(SessionStatus.Running, viewModel.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN StopClicked intent THEN state returns to Idle`() = runTest {
        // GIVEN
        val viewModel = providesHomeViewModel()
        viewModel.handleIntent(HomeIntent.StartClicked)
        viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
        runCurrent()

        // WHEN
        viewModel.handleIntent(HomeIntent.StopClicked)
        runCurrent()

        // THEN
        Assert.assertEquals(SessionStatus.Idle, viewModel.state.value.status)
    }

    @Test
    fun `GIVEN Running state WHEN StopClicked THEN elapsedSeconds and distractionCount are reset`() =
        runTest {
            // GIVEN
            val fakeMonitor = providesFakeDistractionMonitor()
            val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()
            fakeMonitor.emit(DistractionEvent.Noise)
            runCurrent()

            // WHEN
            viewModel.handleIntent(HomeIntent.StopClicked)
            runCurrent()

            // THEN
            Assert.assertEquals(0, viewModel.state.value.elapsedSeconds)
            Assert.assertEquals(0, viewModel.state.value.distractionCount)
            Assert.assertNull(viewModel.state.value.lastDistractionEvent)
        }

    @Test
    fun `GIVEN repository failure WHEN StopClicked THEN FailedToSaveSession effect is sent`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel(
                stopResult = Result.failure(Exception("DB write failed"))
            )
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            val effects = mutableListOf<HomeEffect>()
            val job = launch { viewModel.effects.toList(effects) }

            // WHEN
            viewModel.handleIntent(HomeIntent.StopClicked)
            runCurrent()

            // THEN
            Assert.assertTrue(effects.contains(HomeEffect.FailedToSaveSession))
            job.cancel()
        }

    @Test
    fun `GIVEN Running session WHEN distraction event emitted THEN distractionCount increments`() =
        runTest {
            // GIVEN
            val fakeMonitor = providesFakeDistractionMonitor()
            val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // WHEN
            fakeMonitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            Assert.assertEquals(1, viewModel.state.value.distractionCount)
        }

    @Test
    fun `GIVEN Running session WHEN multiple distraction events emitted THEN distractionCount reflects all events`() =
        runTest {
            // GIVEN
            val fakeMonitor = providesFakeDistractionMonitor()
            val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // WHEN
            fakeMonitor.emit(DistractionEvent.Movement)
            fakeMonitor.emit(DistractionEvent.Noise)
            fakeMonitor.emit(DistractionEvent.Movement)
            runCurrent()

            // THEN
            Assert.assertEquals(3, viewModel.state.value.distractionCount)
        }

    @Test
    fun `GIVEN Running session WHEN distraction event emitted THEN lastDistractionEvent is updated`() =
        runTest {
            // GIVEN
            val fakeMonitor = providesFakeDistractionMonitor()
            val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // WHEN
            fakeMonitor.emit(DistractionEvent.Noise)
            runCurrent()

            // THEN
            Assert.assertEquals(DistractionEvent.Noise, viewModel.state.value.lastDistractionEvent)
        }

    @Test
    fun `GIVEN zero distractions WHEN state is read THEN shieldStrength is 100`() = runTest {
        // GIVEN / WHEN
        val viewModel = providesHomeViewModel()

        // THEN
        Assert.assertEquals(100, viewModel.state.value.shieldStrength)
    }

    @Test
    fun `GIVEN 5 distractions WHEN state is read THEN shieldStrength is 50`() = runTest {
        // GIVEN
        val fakeMonitor = providesFakeDistractionMonitor()
        val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
        viewModel.handleIntent(HomeIntent.StartClicked)
        viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
        runCurrent()

        // WHEN
        repeat(5) { fakeMonitor.emit(DistractionEvent.Movement) }
        runCurrent()

        // THEN
        Assert.assertEquals(50, viewModel.state.value.shieldStrength)
    }

    @Test
    fun `GIVEN 11 or more distractions WHEN state is read THEN shieldStrength is clamped to 0`() =
        runTest {
            // GIVEN
            val fakeMonitor = providesFakeDistractionMonitor()
            val viewModel = providesHomeViewModel(distractionMonitor = fakeMonitor)
            viewModel.handleIntent(HomeIntent.StartClicked)
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = true))
            runCurrent()

            // WHEN
            repeat(15) { fakeMonitor.emit(DistractionEvent.Noise) }
            runCurrent()

            // THEN
            Assert.assertEquals(0, viewModel.state.value.shieldStrength)
        }
}
