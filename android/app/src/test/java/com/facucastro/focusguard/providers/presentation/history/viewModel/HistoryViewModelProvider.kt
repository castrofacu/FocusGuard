package com.facucastro.focusguard.providers.presentation.history.viewModel

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.time.TimeProvider
import com.facucastro.focusguard.domain.usecase.GetHistoryUseCase
import com.facucastro.focusguard.presentation.history.viewModel.HistoryViewModel
import com.facucastro.focusguard.providers.domain.repository.providesMockFocusRepository
import com.facucastro.focusguard.providers.domain.time.providesFakeTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

const val TODAY_MILLIS = 1712750400000L                         // 2024-04-10 12:00 UTC
const val TODAY_MORNING_MILLIS = TODAY_MILLIS - 4 * 3_600_000L  // 2024-04-10 08:00 UTC
const val YESTERDAY_MILLIS = TODAY_MILLIS - 24 * 3_600_000L     // 2024-04-09 12:00 UTC
const val OLDER_MILLIS = TODAY_MILLIS - 3 * 24 * 3_600_000L     // 2024-04-07 12:00 UTC

fun providesHistoryViewModel(
    historyFlow: Flow<List<FocusSession>> = emptyFlow(),
    fakeTimeProvider: TimeProvider = providesFakeTimeProvider(timeToReturn = TODAY_MILLIS)
): HistoryViewModel {
    val repository = providesMockFocusRepository(historyFlow = historyFlow)
    val getHistoryUseCase = GetHistoryUseCase(repository)
    return HistoryViewModel(getHistoryUseCase, fakeTimeProvider)
}
