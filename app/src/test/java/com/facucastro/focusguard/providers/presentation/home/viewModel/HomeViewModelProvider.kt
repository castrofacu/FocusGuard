package com.facucastro.focusguard.providers.presentation.home.viewModel

import com.facucastro.focusguard.domain.session.FocusSessionState
import com.facucastro.focusguard.presentation.home.viewModel.HomeViewModel
import com.facucastro.focusguard.providers.domain.session.FakeFocusSessionController
import com.facucastro.focusguard.providers.domain.session.providesFakeFocusSessionController

fun providesHomeViewModel(
    sessionController: FakeFocusSessionController = providesFakeFocusSessionController(),
    initialState: FocusSessionState = FocusSessionState(),
): HomeViewModel {
    val controller = if (sessionController.mutableState.value == FocusSessionState()) {
        FakeFocusSessionController(initialState)
    } else {
        sessionController
    }
    return HomeViewModel(sessionController = controller)
}
