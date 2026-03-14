package com.facucastro.focusguard.presentation.history.viewModel

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.mockFocusRepository
import com.facucastro.focusguard.domain.time.FakeTimeProvider
import com.facucastro.focusguard.domain.usecase.GetHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.ZoneId


const val TODAY_MILLIS = 1712750400000L                         // 2024-04-10 12:00 UTC
const val TODAY_MORNING_MILLIS = TODAY_MILLIS - 4 * 3_600_000L  // 2024-04-10 08:00 UTC
const val YESTERDAY_MILLIS = TODAY_MILLIS - 24 * 3_600_000L     // 2024-04-09 12:00 UTC
const val OLDER_MILLIS = TODAY_MILLIS - 3 * 24 * 3_600_000L     // 2024-04-07 12:00 UTC

fun session(
    id: Long = 1L,
    startTime: Long,
    durationSeconds: Int = 60,
    distractionCount: Int = 0
) = FocusSession(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)

fun createHistoryViewModel(
    historyFlow: Flow<List<FocusSession>> = emptyFlow(),
    fakeTimeProvider: FakeTimeProvider = FakeTimeProvider(
        timeToReturn = TODAY_MILLIS,
        zoneToReturn = ZoneId.of("UTC")
    )
): HistoryViewModel {
    val repository = mockFocusRepository(historyFlow = historyFlow)
    val getHistoryUseCase = GetHistoryUseCase(repository)
    return HistoryViewModel(getHistoryUseCase, fakeTimeProvider)
}
