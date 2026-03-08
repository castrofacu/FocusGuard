package com.facucastro.focusguard.domain.model

sealed class SessionStatus {
    object Idle : SessionStatus()
    object Running : SessionStatus()
    object Paused : SessionStatus()
}
