package com.facucastro.focusguard.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

private const val MOVEMENT_THRESHOLD = 2.5f
private const val TAG = "AccelerometerDistractionMonitor"

class AccelerometerDistractionMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DistractionMonitor, SensorEventListener {

    private val _events = MutableSharedFlow<DistractionEvent>()
    override val events: SharedFlow<DistractionEvent> = _events

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var initialized = false

    private var emitScope: CoroutineScope? = null

    override fun start(scope: CoroutineScope) {
        emitScope = scope
        initialized = false
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        Log.i(TAG, "Started accelerometer monitoring")
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
        emitScope = null
        Log.i(TAG, "Stopped accelerometer monitoring")
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!initialized) {
            lastX = x; lastY = y; lastZ = z
            initialized = true
            return
        }

        // Euclidean distance between consecutive readings captures movement in any direction
        val delta = sqrt(
            (abs(x - lastX) * abs(x - lastX)) +
            (abs(y - lastY) * abs(y - lastY)) +
            (abs(z - lastZ) * abs(z - lastZ))
        )

        lastX = x; lastY = y; lastZ = z

        if (delta > MOVEMENT_THRESHOLD) {
            emitScope?.launch { _events.emit(DistractionEvent.Movement) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}
