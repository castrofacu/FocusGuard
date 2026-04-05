package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.notification.DistractionNotifier
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import com.facucastro.focusguard.domain.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

private const val NOTIFICATION_THROTTLE_MILLIS = 2_000L

class ObserveDistractionsUseCase @Inject constructor(
    private val distractionMonitor: DistractionMonitor,
    private val distractionNotifier: DistractionNotifier,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(): Flow<DistractionEvent> {
        var lastNotifiedAt = -NOTIFICATION_THROTTLE_MILLIS
        return distractionMonitor.events
            .onStart { distractionMonitor.start() }
            .onCompletion { distractionMonitor.stop() }
            .onEach { event ->
                val now = timeProvider.getCurrentTimeMillis()
                if (now - lastNotifiedAt >= NOTIFICATION_THROTTLE_MILLIS) {
                    lastNotifiedAt = now
                    distractionNotifier.notifyDistraction(event)
                }
            }
    }
}
