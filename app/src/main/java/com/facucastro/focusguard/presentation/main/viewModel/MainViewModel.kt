package com.facucastro.focusguard.presentation.main.viewModel

import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.feature.FeatureFlagService
import com.facucastro.focusguard.domain.feature.FeatureFlags
import com.facucastro.focusguard.domain.usecase.ObserveLoginStateUseCase
import com.facucastro.focusguard.presentation.core.viewmodel.BaseMviViewModel
import com.facucastro.focusguard.presentation.main.contract.MainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeLoginStateUseCase: ObserveLoginStateUseCase,
    private val featureFlagService: FeatureFlagService,
) : BaseMviViewModel<MainState, Nothing, Nothing>(MainState()) {

    init {
        observeLoginState()
        loadFeatureFlags()
    }

    override fun handleIntent(intent: Nothing): Unit = Unit

    private fun observeLoginState() {
        viewModelScope.launch {
            observeLoginStateUseCase().collect { isLoggedIn ->
                setState { copy(isUserLoggedIn = isLoggedIn) }
            }
        }
    }

    private fun loadFeatureFlags() {
        viewModelScope.launch {
            val leaderboardEnabled = featureFlagService.isEnabled(FeatureFlags.LEADERBOARD)
            setState { copy(isLeaderboardEnabled = leaderboardEnabled) }
        }
    }
}
