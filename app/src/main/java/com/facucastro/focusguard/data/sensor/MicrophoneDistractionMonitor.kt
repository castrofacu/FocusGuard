package com.facucastro.focusguard.data.sensor

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.log10

private const val NOISE_THRESHOLD_DB = 70.0
private const val POLL_INTERVAL_MS = 500L
private const val TAG = "MicrophoneDistractionMonitor"

class MicrophoneDistractionMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DistractionMonitor {

    private val _events = MutableSharedFlow<DistractionEvent>()
    override val events: SharedFlow<DistractionEvent> = _events

    private var recorder: MediaRecorder? = null
    private var pollingJob: Job? = null

    override fun start(scope: CoroutineScope) {
        // If recorder setup fails (e.g. RECORD_AUDIO permission denied), the monitor simply
        // produces no events rather than crashing.
        try {
            recorder = createRecorder().also {
                it.prepare()
                it.start()
            }
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            tempOutputFile.delete()
            Log.e(TAG, "Failed to start microphone monitoring", e)
            return
        }

        pollingJob = scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                val amplitude = recorder?.maxAmplitude ?: break
                if (amplitude > 0) {
                    // Convert raw 16-bit PCM amplitude to approximate dB SPL.
                    // Yields ~0 dB at amplitude 1 and ~90 dB at full scale (32767).
                    val decibels = 20 * log10(amplitude.toDouble())
                    if (decibels >= NOISE_THRESHOLD_DB) {
                        _events.emit(DistractionEvent.Noise)
                    }
                }
            }
        }

        Log.i(TAG, "Started microphone monitoring")
    }

    override fun stop() {
        pollingJob?.cancel()
        pollingJob = null
        recorder?.runCatching {
            stop()
            release()
        }
        recorder = null
        tempOutputFile.delete()
        Log.i(TAG, "Stopped microphone monitoring")
    }

    private val tempOutputFile = File(context.cacheDir, "mic_monitor_tmp.3gp")

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(tempOutputFile.absolutePath)
        }
    }
}
