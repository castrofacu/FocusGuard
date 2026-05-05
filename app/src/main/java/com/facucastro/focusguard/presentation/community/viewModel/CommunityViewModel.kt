package com.facucastro.focusguard.presentation.community.viewModel

import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.repository.CommunityRepository
import com.facucastro.focusguard.presentation.community.contract.CommunityEffect
import com.facucastro.focusguard.presentation.community.contract.CommunityIntent
import com.facucastro.focusguard.presentation.community.contract.CommunityState
import com.facucastro.focusguard.presentation.core.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
) : BaseMviViewModel<CommunityState, CommunityIntent, CommunityEffect>(CommunityState()) {

    init {
        handleIntent(CommunityIntent.LoadRanking)
    }

    override fun handleIntent(intent: CommunityIntent) {
        when (intent) {
            CommunityIntent.LoadRanking -> loadRanking()
            CommunityIntent.RetryClicked -> loadRanking()
        }
    }

    private fun loadRanking() {
        if (state.value.isLoading) return

        setState { copy(isLoading = true, errorMessage = null, rankings = emptyList()) }

        viewModelScope.launch {
            communityRepository.getWeeklyRanking()
                .onSuccess { rankings ->
                    setState { copy(isLoading = false, rankings = rankings) }
                }
                .onFailure { error ->
                    val message = error.message ?: "Could not load the leaderboard."
                    setState { copy(isLoading = false, errorMessage = message) }
                    sendEffect(CommunityEffect.ShowErrorSnackbar(message))
                }
        }
    }
}
