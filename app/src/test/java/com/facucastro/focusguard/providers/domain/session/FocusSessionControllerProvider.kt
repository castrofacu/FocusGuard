package com.facucastro.focusguard.providers.domain.session

import com.facucastro.focusguard.domain.session.FocusSessionController
import com.facucastro.focusguard.domain.session.FocusSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFocusSessionController(
    initialState: FocusSessionState = FocusSessionState(),
) : FocusSessionController {

    val mutableState = MutableStateFlow(initialState)
    override val state: StateFlow<FocusSessionState> = mutableState.asStateFlow()

    override fun start()  {}
    override fun pause()  {}
    override fun resume() {}
    override fun stop()   {}
}

fun providesFakeFocusSessionController(
    initialState: FocusSessionState = FocusSessionState(),
): FakeFocusSessionController = FakeFocusSessionController(initialState)
