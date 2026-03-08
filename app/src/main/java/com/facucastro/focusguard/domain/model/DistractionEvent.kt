package com.facucastro.focusguard.domain.model

sealed class DistractionEvent {
    object Movement : DistractionEvent()
    object Noise : DistractionEvent()
}
