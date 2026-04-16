package com.facucastro.focusguard.presentation.home.viewModel

import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.session.FocusSessionController
import com.facucastro.focusguard.domain.session.FocusSessionState
import com.facucastro.focusguard.presentation.core.viewmodel.BaseMviViewModel
import com.facucastro.focusguard.presentation.home.contract.HomeEffect
import com.facucastro.focusguard.presentation.home.contract.HomeIntent
import com.facucastro.focusguard.presentation.home.contract.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionController: FocusSessionController,
) : BaseMviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init {
        sessionController.state
            .onEach { sessionState -> setState { sessionState.toHomeState() } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.StartClicked -> onStartClicked()
            is HomeIntent.PermissionsResult -> onPermissionsResult(intent.isNotificationGranted)
            HomeIntent.PauseClicked -> onPauseClicked()
            HomeIntent.ResumeClicked -> onResumeClicked()
            HomeIntent.StopClicked -> onStopClicked()
        }
    }

    private fun onStartClicked() {
        if (state.value.status != SessionStatus.Idle) return
        launchEffect(HomeEffect.RequestPermissions)
    }

    private fun onPermissionsResult(isNotificationGranted: Boolean) {
        if (state.value.status != SessionStatus.Idle) return
        if (!isNotificationGranted) {
            launchEffect(HomeEffect.NotificationsPermissionDenied)
        }
        launchEffect(HomeEffect.StartSessionService)
    }

    private fun onPauseClicked() = launchEffect(HomeEffect.PauseSessionService)
    private fun onResumeClicked() = launchEffect(HomeEffect.ResumeSessionService)
    private fun onStopClicked() = launchEffect(HomeEffect.StopSessionService)

    private fun FocusSessionState.toHomeState(): HomeState = HomeState(
        status = status,
        elapsedSeconds = elapsedSeconds,
        distractionCount = distractionCount,
        lastDistractionEvent = lastDistractionEvent,
    )
}
