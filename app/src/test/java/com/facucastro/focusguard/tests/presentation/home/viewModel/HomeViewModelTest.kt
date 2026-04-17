package com.facucastro.focusguard.tests.presentation.home.viewModel

import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.presentation.home.contract.HomeEffect
import com.facucastro.focusguard.presentation.home.contract.HomeIntent
import com.facucastro.focusguard.presentation.home.contract.HomeState
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
    fun `GIVEN Idle state WHEN StartClicked twice without PermissionsResult THEN RequestPermissions is emitted each time`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()

            // Subscribe before any intents so the channel buffer is fully drained.
            val effects = mutableListOf<HomeEffect>()
            val job = launch { viewModel.effects.toList(effects) }

            // WHEN — two StartClicked intents without a PermissionsResult in between.
            // State remains Idle because the service is not running in unit tests, so the
            // guard fires and RequestPermissions is emitted each time.
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            // THEN
            Assert.assertEquals(2, effects.count { it is HomeEffect.RequestPermissions })
            job.cancel()
        }

    @Test
    fun `GIVEN StartClicked WHEN PermissionsResult denied THEN NotificationsPermissionDenied effect is sent`() =
        runTest {
            // GIVEN
            val viewModel = providesHomeViewModel()
            viewModel.handleIntent(HomeIntent.StartClicked)
            runCurrent()

            val effects = mutableListOf<HomeEffect>()
            val job = launch { viewModel.effects.toList(effects) }

            // WHEN
            viewModel.handleIntent(HomeIntent.PermissionsResult(isNotificationGranted = false, isMicrophoneGranted = true))
            runCurrent()

            // THEN
            Assert.assertTrue(effects.contains(HomeEffect.NotificationsPermissionDenied))
            job.cancel()
        }

    @Test
    fun `GIVEN zero distractions WHEN state is read THEN shieldStrength is 100`() = runTest {
        // GIVEN / WHEN
        val viewModel = providesHomeViewModel()

        // THEN
        Assert.assertEquals(100, viewModel.state.value.shieldStrength)
    }
}
