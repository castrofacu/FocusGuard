package com.facucastro.focusguard.data.sensor

import android.util.Log
import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CompositeDistractionMonitor"

@Singleton
class CompositeDistractionMonitor @Inject constructor(
    private val accelerometerMonitor: AccelerometerDistractionMonitor,
    private val microphoneMonitor: MicrophoneDistractionMonitor
) : DistractionMonitor {

    private val _events = MutableSharedFlow<DistractionEvent>()
    override val events: SharedFlow<DistractionEvent> = _events

    override fun start(scope: CoroutineScope) {
        accelerometerMonitor.start(scope)
        microphoneMonitor.start(scope)

        scope.launch {
            merge(accelerometerMonitor.events, microphoneMonitor.events)
                .collect { _events.emit(it) }
        }

        Log.i(TAG, "Started composite monitoring")
    }

    override fun stop() {
        accelerometerMonitor.stop()
        microphoneMonitor.stop()
        Log.i(TAG, "Stopped composite monitoring")
    }
}
