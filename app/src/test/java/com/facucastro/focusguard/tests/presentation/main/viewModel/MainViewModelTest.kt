package com.facucastro.focusguard.tests.presentation.main.viewModel

import com.facucastro.focusguard.providers.presentation.main.viewModel.providesMainViewModel
import com.facucastro.focusguard.providers.presentation.main.viewModel.providesMainViewModelWithLeaderboard
import com.facucastro.focusguard.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `GIVEN initial state WHEN created THEN isUserLoggedIn is false and isLeaderboardEnabled is false`() = runTest {
        // GIVEN / WHEN
        val viewModel = providesMainViewModel()
        runCurrent()

        // THEN
        val state = viewModel.state.value
        assertFalse(state.isUserLoggedIn)
        assertFalse(state.isLeaderboardEnabled)
    }

    @Test
    fun `GIVEN leaderboard flag is false WHEN init THEN state isLeaderboardEnabled is false`() = runTest {
        // GIVEN
        val viewModel = providesMainViewModelWithLeaderboard(enabled = false)

        // WHEN
        runCurrent()

        // THEN
        assertFalse(viewModel.state.value.isLeaderboardEnabled)
    }

    @Test
    fun `GIVEN leaderboard flag is true WHEN init THEN state isLeaderboardEnabled is true`() = runTest {
        // GIVEN
        val viewModel = providesMainViewModelWithLeaderboard(enabled = true)

        // WHEN
        runCurrent()

        // THEN
        assertTrue(viewModel.state.value.isLeaderboardEnabled)
    }

    @Test
    fun `GIVEN user is logged in WHEN observing state THEN isUserLoggedIn is true`() = runTest {
        // GIVEN
        val viewModel = providesMainViewModel(isUserLoggedIn = kotlinx.coroutines.flow.flowOf(true))

        // WHEN
        runCurrent()

        // THEN
        assertTrue(viewModel.state.value.isUserLoggedIn)
    }
}
