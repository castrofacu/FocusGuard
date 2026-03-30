package com.facucastro.focusguard.tests.presentation.history.viewModel

import com.facucastro.focusguard.presentation.history.state.HistoryUiState
import com.facucastro.focusguard.presentation.history.viewModel.HistoryViewModel
import com.facucastro.focusguard.providers.presentation.history.viewModel.OLDER_MILLIS
import com.facucastro.focusguard.providers.presentation.history.viewModel.TODAY_MILLIS
import com.facucastro.focusguard.providers.presentation.history.viewModel.TODAY_MORNING_MILLIS
import com.facucastro.focusguard.providers.presentation.history.viewModel.YESTERDAY_MILLIS
import com.facucastro.focusguard.providers.presentation.history.viewModel.providesHistoryViewModel
import com.facucastro.focusguard.providers.domain.model.providesFocusSession
import com.facucastro.focusguard.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    @Test
    fun `GIVEN viewModel just created WHEN no emission yet THEN initial state is loading`() =
        runTest {
            // GIVEN / WHEN
            val viewModel = providesHistoryViewModel(historyFlow = emptyFlow())

            // THEN
            Assert.assertEquals(
                HistoryUiState(isLoading = true, zoneId = ZoneId.of("UTC")),
                viewModel.uiState.value
            )
        }

    @Test
    fun `GIVEN empty session list WHEN flow emits THEN state is not loading with empty groups and zero stats`() =
        runTest {
            // GIVEN
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(emptyList()))

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val finalState = viewModel.uiState.value

            // THEN
            Assert.assertFalse(finalState.isLoading)
            Assert.assertEquals(emptyList<HistoryUiState.SessionGroup>(), finalState.sessionGroups)
            Assert.assertEquals(0, finalState.totalSessions)
            Assert.assertEquals(0, finalState.totalFocusMinutes)
            Assert.assertEquals(0f, finalState.avgDistractions)
        }

    @Test
    fun `GIVEN session from today WHEN flow emits THEN group is labeled Today`() = runTest {
        // GIVEN
        val viewModel = providesHistoryViewModel(
            historyFlow = flowOf(listOf(providesFocusSession(startTime = TODAY_MORNING_MILLIS)))
        )

        // WHEN
        startCollecting(viewModel)
        runCurrent()
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        Assert.assertEquals(HistoryUiState.DateLabel.Today, group.dateLabel)
    }

    @Test
    fun `GIVEN session from yesterday WHEN flow emits THEN group is labeled Yesterday`() = runTest {
        // GIVEN
        val viewModel = providesHistoryViewModel(
            historyFlow = flowOf(listOf(providesFocusSession(startTime = YESTERDAY_MILLIS)))
        )

        // WHEN
        startCollecting(viewModel)
        runCurrent()
        val group = viewModel.uiState.value.sessionGroups.single()

        // THEN
        Assert.assertEquals(HistoryUiState.DateLabel.Yesterday, group.dateLabel)
    }

    @Test
    fun `GIVEN session from older date WHEN flow emits THEN group is labeled Other with correct date`() =
        runTest {
            // GIVEN
            val viewModel = providesHistoryViewModel(
                historyFlow = flowOf(listOf(providesFocusSession(startTime = OLDER_MILLIS)))
            )

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val group = viewModel.uiState.value.sessionGroups.single()

            // THEN
            Assert.assertEquals(
                HistoryUiState.DateLabel.Other(LocalDate.of(2024, 4, 7)),
                group.dateLabel
            )
        }

    @Test
    fun `GIVEN sessions from multiple days WHEN flow emits THEN groups are sorted most recent first`() =
        runTest {
            // GIVEN
            val sessions = listOf(
                providesFocusSession(id = 1L, startTime = OLDER_MILLIS),
                providesFocusSession(id = 2L, startTime = YESTERDAY_MILLIS),
                providesFocusSession(id = 3L, startTime = TODAY_MORNING_MILLIS)
            )
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(sessions))

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val groups = viewModel.uiState.value.sessionGroups

            // THEN
            Assert.assertEquals(3, groups.size)
            Assert.assertEquals(HistoryUiState.DateLabel.Today, groups[0].dateLabel)
            Assert.assertEquals(HistoryUiState.DateLabel.Yesterday, groups[1].dateLabel)
            Assert.assertEquals(
                HistoryUiState.DateLabel.Other(LocalDate.of(2024, 4, 7)),
                groups[2].dateLabel
            )
        }

    @Test
    fun `GIVEN multiple sessions on same day WHEN flow emits THEN sessions within group are sorted newest first`() =
        runTest {
            // GIVEN
            val earlier = providesFocusSession(id = 1L, startTime = TODAY_MORNING_MILLIS)
            val later = providesFocusSession(id = 2L, startTime = TODAY_MORNING_MILLIS + 3_600_000L)
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(listOf(earlier, later)))

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val daySessions = viewModel.uiState.value.sessionGroups.single().sessions

            // THEN
            Assert.assertEquals(later, daySessions[0])
            Assert.assertEquals(earlier, daySessions[1])
        }

    @Test
    fun `GIVEN sessions with known durations WHEN flow emits THEN totalFocusMinutes is total seconds divided by 60`() =
        runTest {
            // GIVEN
            val sessions = listOf(
                providesFocusSession(
                    id = 1L,
                    startTime = TODAY_MORNING_MILLIS,
                    durationSeconds = 300
                ),  // 5 min
                providesFocusSession(
                    id = 2L,
                    startTime = YESTERDAY_MILLIS,
                    durationSeconds = 1800
                ), // 30 min
            )
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(sessions))

            // WHEN
            startCollecting(viewModel)
            runCurrent()

            // THEN
            Assert.assertEquals(35, viewModel.uiState.value.totalFocusMinutes)
        }

    @Test
    fun `GIVEN sessions with known distractions WHEN flow emits THEN avgDistractions is the correct average`() =
        runTest {
            // GIVEN
            val sessions = listOf(
                providesFocusSession(
                    id = 1L,
                    startTime = TODAY_MORNING_MILLIS,
                    distractionCount = 2
                ),
                providesFocusSession(id = 2L, startTime = YESTERDAY_MILLIS, distractionCount = 4),
            )
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(sessions))

            // WHEN
            startCollecting(viewModel)
            runCurrent()

            // THEN
            Assert.assertEquals(3f, viewModel.uiState.value.avgDistractions)
        }

    @Test
    fun `GIVEN multiple sessions WHEN flow emits THEN totalSessions equals the session count`() =
        runTest {
            // GIVEN
            val sessions = listOf(
                providesFocusSession(id = 1L, startTime = TODAY_MORNING_MILLIS),
                providesFocusSession(id = 2L, startTime = YESTERDAY_MILLIS),
                providesFocusSession(id = 3L, startTime = OLDER_MILLIS),
            )
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(sessions))

            // WHEN
            startCollecting(viewModel)
            runCurrent()

            // THEN
            Assert.assertEquals(3, viewModel.uiState.value.totalSessions)
        }

    @Test
    fun `GIVEN sessions with known distractions WHEN flow emits THEN totalDistractions is sum of all distraction counts`() =
        runTest {
            // GIVEN
            val sessions = listOf(
                providesFocusSession(id = 1L, startTime = TODAY_MORNING_MILLIS, distractionCount = 3),
                providesFocusSession(id = 2L, startTime = YESTERDAY_MILLIS, distractionCount = 5),
            )
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(sessions))

            // WHEN
            startCollecting(viewModel)
            runCurrent()

            // THEN
            Assert.assertEquals(8, viewModel.uiState.value.totalDistractions)
        }

    @Test
    fun `GIVEN no sessions WHEN flow emits THEN totalDistractions is zero`() = runTest {
        // GIVEN
        val viewModel = providesHistoryViewModel(historyFlow = flowOf(emptyList()))

        // WHEN
        startCollecting(viewModel)
        runCurrent()

        // THEN
        Assert.assertEquals(0, viewModel.uiState.value.totalDistractions)
    }

    @Test
    fun `GIVEN any sessions WHEN flow emits THEN weeklyMinutesByDay has 7 entries with correct day labels`() =
        runTest {
            // GIVEN
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(emptyList()))

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val days = viewModel.uiState.value.weeklyMinutesByDay

            // THEN — today is 2024-04-10 (Wed), window starts Thu Apr 4
            Assert.assertEquals(7, days.size)
            Assert.assertEquals(
                listOf("THU", "FRI", "SAT", "SUN", "MON", "TUE", "WED"),
                days.map { it.first }
            )
        }

    @Test
    fun `GIVEN session today WHEN flow emits THEN weeklyMinutesByDay last entry has correct minutes`() =
        runTest {
            // GIVEN — 3600 seconds = 60 minutes
            val viewModel = providesHistoryViewModel(
                historyFlow = flowOf(
                    listOf(providesFocusSession(startTime = TODAY_MORNING_MILLIS, durationSeconds = 3600))
                )
            )

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val days = viewModel.uiState.value.weeklyMinutesByDay

            // THEN
            Assert.assertEquals(Pair("WED", 60), days.last())
        }

    @Test
    fun `GIVEN session yesterday WHEN flow emits THEN weeklyMinutesByDay second-to-last entry has correct minutes`() =
        runTest {
            // GIVEN — 1800 seconds = 30 minutes
            val viewModel = providesHistoryViewModel(
                historyFlow = flowOf(
                    listOf(providesFocusSession(startTime = YESTERDAY_MILLIS, durationSeconds = 1800))
                )
            )

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val days = viewModel.uiState.value.weeklyMinutesByDay

            // THEN — yesterday is TUE at index 5, today (WED) has 0
            Assert.assertEquals(Pair("TUE", 30), days[5])
            Assert.assertEquals(Pair("WED", 0), days[6])
        }

    @Test
    fun `GIVEN sessions on different days in week WHEN flow emits THEN weeklyMinutesByDay sums minutes per day correctly`() =
        runTest {
            // GIVEN
            val sessions = listOf(
                providesFocusSession(id = 1L, startTime = TODAY_MORNING_MILLIS, durationSeconds = 1200),  // 20 min, WED
                providesFocusSession(id = 2L, startTime = TODAY_MORNING_MILLIS + 3_600_000L, durationSeconds = 1200), // 20 min, WED
                providesFocusSession(id = 3L, startTime = YESTERDAY_MILLIS, durationSeconds = 3000),      // 50 min, TUE
            )
            val viewModel = providesHistoryViewModel(historyFlow = flowOf(sessions))

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val days = viewModel.uiState.value.weeklyMinutesByDay

            // THEN
            Assert.assertEquals(Pair("TUE", 50), days[5])
            Assert.assertEquals(Pair("WED", 40), days[6])
        }

    @Test
    fun `GIVEN session older than 7 days WHEN flow emits THEN weeklyMinutesByDay shows all zeros`() =
        runTest {
            // GIVEN — 8 days before today
            val eightDaysAgoMillis = TODAY_MILLIS - 8 * 24 * 3_600_000L
            val viewModel = providesHistoryViewModel(
                historyFlow = flowOf(
                    listOf(providesFocusSession(startTime = eightDaysAgoMillis, durationSeconds = 3600))
                )
            )

            // WHEN
            startCollecting(viewModel)
            runCurrent()
            val days = viewModel.uiState.value.weeklyMinutesByDay

            // THEN
            Assert.assertTrue(days.all { it.second == 0 })
        }


    private fun TestScope.startCollecting(viewModel: HistoryViewModel) {
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }
    }
}
