package com.facucastro.focusguard.domain.sensor

import com.facucastro.focusguard.domain.model.DistractionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

interface DistractionMonitor {
    val events: SharedFlow<DistractionEvent>
    fun start(scope: CoroutineScope)
    fun stop()
}
