package com.facucastro.focusguard.presentation.history.viewModel

import com.facucastro.focusguard.presentation.history.state.HistoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN viewModel just created WHEN no emission yet THEN initial state is loading`() = runTest {
        // GIVEN / WHEN
        val viewModel = createHistoryViewModel(historyFlow = emptyFlow())

        // THEN
        assertEquals(HistoryUiState(isLoading = true), viewModel.uiState.value)
    }

    @Test
    fun `GIVEN empty session list WHEN flow emits THEN state is not loading with empty groups and zero stats`() = runTest {
        // GIVEN
        val viewModel = createHistoryViewModel(historyFlow = flowOf(emptyList()))

        // WHEN
        startCollecting(viewModel)
        val finalState = viewModel.uiState.value

        // THEN
        assertFalse(finalState.isLoading)
        assertEquals(emptyList<HistoryUiState.SessionGroup>(), finalState.sessionGroups)
        assertEquals(0, finalState.totalSessions)
        assertEquals(0, finalState.totalFocusMinutes)
        assertEquals(0f, finalState.avgDistractions)
    }

    @Test
    fun `GIVEN session from today WHEN flow emits THEN group is labeled Today`() = runTest {
        // GIVEN
        val viewModel = createHistoryViewModel(
            historyFlow = flowOf(listOf(session(startTime = TODAY_MORNING_MILLIS)))
        )

        // WHEN
        startCollecting(viewModel)
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        assertEquals(HistoryUiState.DateLabel.Today, group.dateLabel)
    }

    @Test
    fun `GIVEN session from yesterday WHEN flow emits THEN group is labeled Yesterday`() = runTest {
        // GIVEN
        val viewModel = createHistoryViewModel(
            historyFlow = flowOf(listOf(session(startTime = YESTERDAY_MILLIS)))
        )

        // WHEN
        startCollecting(viewModel)
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        assertEquals(HistoryUiState.DateLabel.Yesterday, group.dateLabel)
    }

    @Test
    fun `GIVEN session from older date WHEN flow emits THEN group is labeled Other with correct date`() = runTest {
        // GIVEN
        val viewModel = createHistoryViewModel(
            historyFlow = flowOf(listOf(session(startTime = OLDER_MILLIS)))
        )

        // WHEN
        startCollecting(viewModel)
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        assertEquals(
            HistoryUiState.DateLabel.Other(LocalDate.of(2024, 4, 7)),
            group.dateLabel
        )
    }

    @Test
    fun `GIVEN sessions from multiple days WHEN flow emits THEN groups are sorted most recent first`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = OLDER_MILLIS),
            session(id = 2L, startTime = YESTERDAY_MILLIS),
            session(id = 3L, startTime = TODAY_MORNING_MILLIS)
        )
        val viewModel = createHistoryViewModel(historyFlow = flowOf(sessions))

        // WHEN
        startCollecting(viewModel)
        val groups = viewModel.uiState.value.sessionGroups

        // THEN
        assertEquals(3, groups.size)
        assertEquals(HistoryUiState.DateLabel.Today, groups[0].dateLabel)
        assertEquals(HistoryUiState.DateLabel.Yesterday, groups[1].dateLabel)
        assertEquals(HistoryUiState.DateLabel.Other(LocalDate.of(2024, 4, 7)), groups[2].dateLabel)
    }

    @Test
    fun `GIVEN multiple sessions on same day WHEN flow emits THEN sessions within group are sorted newest first`() = runTest {
        // GIVEN
        val earlier = session(id = 1L, startTime = TODAY_MORNING_MILLIS)
        val later = session(id = 2L, startTime = TODAY_MORNING_MILLIS + 3_600_000L)
        val viewModel = createHistoryViewModel(historyFlow = flowOf(listOf(earlier, later)))

        // WHEN
        startCollecting(viewModel)
        val daySessions = viewModel.uiState.value.sessionGroups.single().sessions

        // THEN
        assertEquals(later, daySessions[0])
        assertEquals(earlier, daySessions[1])
    }

    @Test
    fun `GIVEN sessions with known durations WHEN flow emits THEN totalFocusMinutes is total seconds divided by 60`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = TODAY_MORNING_MILLIS, durationSeconds = 300),  // 5 min
            session(id = 2L, startTime = YESTERDAY_MILLIS,    durationSeconds = 1800), // 30 min
        )
        val viewModel = createHistoryViewModel(historyFlow = flowOf(sessions))

        // WHEN
        startCollecting(viewModel)

        // THEN
        assertEquals(35, viewModel.uiState.value.totalFocusMinutes)
    }

    @Test
    fun `GIVEN sessions with known distractions WHEN flow emits THEN avgDistractions is the correct average`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = TODAY_MORNING_MILLIS, distractionCount = 2),
            session(id = 2L, startTime = YESTERDAY_MILLIS,    distractionCount = 4),
        )
        val viewModel = createHistoryViewModel(historyFlow = flowOf(sessions))

        // WHEN
        startCollecting(viewModel)

        // THEN
        assertEquals(3f, viewModel.uiState.value.avgDistractions)
    }

    @Test
    fun `GIVEN multiple sessions WHEN flow emits THEN totalSessions equals the session count`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = TODAY_MORNING_MILLIS),
            session(id = 2L, startTime = YESTERDAY_MILLIS),
            session(id = 3L, startTime = OLDER_MILLIS),
        )
        val viewModel = createHistoryViewModel(historyFlow = flowOf(sessions))

        // WHEN
        startCollecting(viewModel)

        // THEN
        assertEquals(3, viewModel.uiState.value.totalSessions)
    }


    private fun TestScope.startCollecting(viewModel: HistoryViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }
}
