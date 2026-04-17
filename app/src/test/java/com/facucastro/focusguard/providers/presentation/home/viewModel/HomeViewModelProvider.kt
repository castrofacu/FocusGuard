package com.facucastro.focusguard.providers.presentation.home.viewModel

import com.facucastro.focusguard.presentation.home.viewModel.HomeViewModel
import com.facucastro.focusguard.providers.domain.session.FakeFocusSessionController
import com.facucastro.focusguard.providers.domain.session.providesFakeFocusSessionController

fun providesHomeViewModel(
    sessionController: FakeFocusSessionController = providesFakeFocusSessionController(),
): HomeViewModel = HomeViewModel(sessionController = sessionController)
