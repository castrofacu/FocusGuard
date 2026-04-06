package com.facucastro.focusguard.providers.domain.sensor

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class FakeDistractionMonitor : DistractionMonitor {
    private val _events = MutableSharedFlow<DistractionEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<DistractionEvent> = _events

    override fun start() { /* no-op */ }
    override fun stop() { /* no-op */ }

    suspend fun emit(event: DistractionEvent) = _events.emit(event)
}

fun providesFakeDistractionMonitor() = FakeDistractionMonitor()