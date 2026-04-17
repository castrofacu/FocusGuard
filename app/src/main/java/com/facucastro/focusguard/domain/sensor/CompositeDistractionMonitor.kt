package com.facucastro.focusguard.domain.sensor

import android.util.Log
import com.facucastro.focusguard.domain.model.DistractionEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

private const val TAG = "CompositeDistractionMonitor"

class CompositeDistractionMonitor(
    private val monitors: List<DistractionMonitor>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : DistractionMonitor {

    private val _events = MutableSharedFlow<DistractionEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<DistractionEvent> = _events

    private var mergeScope: CoroutineScope? = null

    override fun start() {
        monitors.forEach { it.start() }

        mergeScope?.cancel()
        mergeScope = CoroutineScope(SupervisorJob() + dispatcher)
        mergeScope?.launch {
            merge(*monitors.map { it.events }.toTypedArray())
                .collect { _events.emit(it) }
        }

        Log.i(TAG, "Started composite monitoring (${monitors.size} monitors)")
    }

    override fun stop() {
        monitors.forEach { it.stop() }
        mergeScope?.cancel()
        mergeScope = null
        Log.i(TAG, "Stopped composite monitoring")
    }
}
