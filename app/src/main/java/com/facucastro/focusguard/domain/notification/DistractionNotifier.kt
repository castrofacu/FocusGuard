package com.facucastro.focusguard.domain.notification

import com.facucastro.focusguard.domain.model.DistractionEvent

interface DistractionNotifier {
    fun notifyDistraction(event: DistractionEvent)
}
