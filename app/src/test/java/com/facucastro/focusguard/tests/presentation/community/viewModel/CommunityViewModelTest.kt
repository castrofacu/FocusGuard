package com.facucastro.focusguard.tests.presentation.community.viewModel

import com.facucastro.focusguard.presentation.community.contract.CommunityEffect
import com.facucastro.focusguard.presentation.community.contract.CommunityIntent
import com.facucastro.focusguard.presentation.community.viewModel.CommunityViewModel
import com.facucastro.focusguard.providers.domain.repository.fakeCommunityRankings
import com.facucastro.focusguard.providers.domain.repository.providesFakeCommunityRepository
import com.facucastro.focusguard.providers.presentation.community.viewModel.providesCommunityViewModel
import com.facucastro.focusguard.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `GIVEN success response WHEN init THEN state shows rankings and is not loading`() = runTest {
        // GIVEN
        val viewModel = providesCommunityViewModel(
            rankingResult = Result.success(fakeCommunityRankings),
        )

        // WHEN
        runCurrent()

        // THEN
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(fakeCommunityRankings, state.rankings)
    }

    @Test
    fun `GIVEN error response WHEN LoadRanking intent THEN state has errorMessage and empty rankings`() = runTest {
        // GIVEN
        val errorMessage = "Network timeout"
        val viewModel = providesCommunityViewModel(
            rankingResult = Result.failure(Exception(errorMessage)),
        )

        // WHEN
        runCurrent()

        // THEN
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.rankings.isEmpty())
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun `GIVEN error response WHEN LoadRanking THEN ShowErrorSnackbar effect is emitted`() = runTest {
        // GIVEN
        val collectedEffects = mutableListOf<CommunityEffect>()
        val collectJob = launch {
            providesCommunityViewModel(
                rankingResult = Result.failure(Exception("timeout")),
            ).effects.collect { collectedEffects.add(it) }
        }

        // WHEN
        runCurrent()
        collectJob.cancel()

        // THEN
        assertTrue(collectedEffects.any { it is CommunityEffect.ShowErrorSnackbar })
    }

    @Test
    fun `GIVEN error state WHEN RetryClicked THEN rankings are loaded on success`() = runTest {
        // GIVEN
        val fakeRepo = providesFakeCommunityRepository(
            rankingResult = Result.failure(Exception("error")),
        )
        val viewModel = CommunityViewModel(fakeRepo)
        runCurrent()

        // WHEN
        fakeRepo.rankingResult = Result.success(fakeCommunityRankings)
        viewModel.handleIntent(CommunityIntent.RetryClicked)
        runCurrent()

        // THEN
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(fakeCommunityRankings, state.rankings)
    }

    @Test
    fun `GIVEN loading in progress WHEN LoadRanking sent again THEN second call is ignored`() = runTest {
        // GIVEN
        val fakeRepo = providesFakeCommunityRepository()
        val viewModel = CommunityViewModel(fakeRepo)

        // WHEN
        viewModel.handleIntent(CommunityIntent.LoadRanking)

        runCurrent()

        // THEN
        assertEquals(1, fakeRepo.callCount)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(fakeCommunityRankings, viewModel.state.value.rankings)
    }
}
