package com.facucastro.focusguard.domain.session

import kotlinx.coroutines.flow.StateFlow

interface FocusSessionController {
    val state: StateFlow<FocusSessionState>
    fun start()
    fun pause()
    fun resume()
    fun stop()
}
