package com.facucastro.focusguard.domain.timer

import com.facucastro.focusguard.domain.time.TimeProvider

class FocusSessionTimer(
    val startTimeMillis: Long,
    private val timeProvider: TimeProvider,
) {
    private var totalPausedMillis: Long = 0L

    @Volatile private var pausedAtMillis: Long? = null

    val isPaused: Boolean get() = pausedAtMillis != null

    fun pause() {
        if (pausedAtMillis != null) return
        pausedAtMillis = timeProvider.getCurrentTimeMillis()
    }

    fun resume() {
        val pausedAt = pausedAtMillis ?: return
        totalPausedMillis += timeProvider.getCurrentTimeMillis() - pausedAt
        pausedAtMillis = null
    }

    fun elapsedSeconds(): Int {
        val effectiveNow = pausedAtMillis ?: timeProvider.getCurrentTimeMillis()
        return ((effectiveNow - startTimeMillis - totalPausedMillis) / 1000).toInt()
    }
}
