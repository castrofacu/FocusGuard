package com.facucastro.focusguard.presentation.history.viewModel

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.mockFocusRepository
import com.facucastro.focusguard.domain.time.FakeTimeProvider
import com.facucastro.focusguard.domain.usecase.GetHistoryUseCase
import com.facucastro.focusguard.presentation.history.state.HistoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
import java.time.ZoneId

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // 2024-04-10 12:00:00 UTC
    private val todayMillis = 1712750400000L
    private val todayMorningMillis = todayMillis - 4 * 3_600_000L       // 2024-04-10 08:00 UTC
    private val yesterdayMillis = todayMillis - 24 * 3_600_000L          // 2024-04-09 12:00 UTC
    private val olderMillis = todayMillis - 3 * 24 * 3_600_000L          // 2024-04-07 12:00 UTC

    private val fakeTimeProvider = FakeTimeProvider(
        timeToReturn = todayMillis,
        zoneToReturn = ZoneId.of("UTC")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        historyFlow: Flow<List<FocusSession>> = emptyFlow()
    ): HistoryViewModel {
        val repository = mockFocusRepository(historyFlow = historyFlow)
        val getHistoryUseCase = GetHistoryUseCase(repository)
        return HistoryViewModel(getHistoryUseCase, fakeTimeProvider)
    }

    private fun session(
        id: Long = 1L,
        startTime: Long,
        durationSeconds: Int = 60,
        distractionCount: Int = 0
    ) = FocusSession(id = id, startTime = startTime, durationSeconds = durationSeconds, distractionCount = distractionCount)

    @Test
    fun `GIVEN viewModel just created WHEN no emission yet THEN initial state is loading`() = runTest {
        // GIVEN / WHEN
        val viewModel = createViewModel(historyFlow = emptyFlow())

        // THEN
        assertEquals(HistoryUiState(isLoading = true), viewModel.uiState.value)
    }

    @Test
    fun `GIVEN empty session list WHEN flow emits THEN state is not loading with empty groups and zero stats`() = runTest {
        // GIVEN
        val viewModel = createViewModel(historyFlow = flowOf(emptyList()))

        // WHEN – backgroundScope keeps the WhileSubscribed subscription alive for the duration of the test
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
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
        val viewModel = createViewModel(historyFlow = flowOf(listOf(session(startTime = todayMorningMillis))))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        assertEquals(HistoryUiState.DateLabel.Today, group.dateLabel)
    }

    @Test
    fun `GIVEN session from yesterday WHEN flow emits THEN group is labeled Yesterday`() = runTest {
        // GIVEN
        val viewModel = createViewModel(historyFlow = flowOf(listOf(session(startTime = yesterdayMillis))))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        assertEquals(HistoryUiState.DateLabel.Yesterday, group.dateLabel)
    }

    @Test
    fun `GIVEN session from older date WHEN flow emits THEN group is labeled Other with correct date`() = runTest {
        // GIVEN
        val viewModel = createViewModel(historyFlow = flowOf(listOf(session(startTime = olderMillis))))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        assertEquals(HistoryUiState.DateLabel.Other(LocalDate.of(2024, 4, 7)), group.dateLabel)
    }

    @Test
    fun `GIVEN sessions from multiple days WHEN flow emits THEN groups are sorted most recent first`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = olderMillis),
            session(id = 2L, startTime = yesterdayMillis),
            session(id = 3L, startTime = todayMorningMillis)
        )
        val viewModel = createViewModel(historyFlow = flowOf(sessions))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
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
        val earlier = session(id = 1L, startTime = todayMorningMillis)
        val later = session(id = 2L, startTime = todayMorningMillis + 3_600_000L)
        val viewModel = createViewModel(historyFlow = flowOf(listOf(earlier, later)))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val daySessions = viewModel.uiState.value.sessionGroups.single().sessions

        // THEN
        assertEquals(later, daySessions[0])
        assertEquals(earlier, daySessions[1])
    }

    @Test
    fun `GIVEN sessions with known durations WHEN flow emits THEN totalFocusMinutes is total seconds divided by 60`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = todayMorningMillis, durationSeconds = 300),  // 5 min
            session(id = 2L, startTime = yesterdayMillis,    durationSeconds = 1800), // 30 min
        )
        val viewModel = createViewModel(historyFlow = flowOf(sessions))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        // THEN
        assertEquals(35, viewModel.uiState.value.totalFocusMinutes)
    }

    @Test
    fun `GIVEN sessions with known distractions WHEN flow emits THEN avgDistractions is the correct average`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = todayMorningMillis, distractionCount = 2),
            session(id = 2L, startTime = yesterdayMillis,    distractionCount = 4),
        )
        val viewModel = createViewModel(historyFlow = flowOf(sessions))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        // THEN
        assertEquals(3f, viewModel.uiState.value.avgDistractions)
    }

    @Test
    fun `GIVEN multiple sessions WHEN flow emits THEN totalSessions equals the session count`() = runTest {
        // GIVEN
        val sessions = listOf(
            session(id = 1L, startTime = todayMorningMillis),
            session(id = 2L, startTime = yesterdayMillis),
            session(id = 3L, startTime = olderMillis),
        )
        val viewModel = createViewModel(historyFlow = flowOf(sessions))

        // WHEN
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        // THEN
        assertEquals(3, viewModel.uiState.value.totalSessions)
    }
}
