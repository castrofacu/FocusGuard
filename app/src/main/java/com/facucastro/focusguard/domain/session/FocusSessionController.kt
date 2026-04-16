package com.facucastro.focusguard.domain.session

import com.facucastro.focusguard.domain.model.FocusSessionState
import kotlinx.coroutines.flow.StateFlow

interface FocusSessionController {
    val state: StateFlow<FocusSessionState>
    fun start()
    fun pause()
    fun resume()
    fun stop()
}
