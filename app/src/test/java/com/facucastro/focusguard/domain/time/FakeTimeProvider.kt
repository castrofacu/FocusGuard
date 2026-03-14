package com.facucastro.focusguard.domain.time

import java.time.ZoneId

class FakeTimeProvider(
    var timeToReturn: Long = 1000L,
    var shouldThrow: Boolean = false,
    var zoneId: ZoneId = ZoneId.of("UTC")
) : TimeProvider {
    override fun getCurrentTimeMillis(): Long {
        if (shouldThrow) throw RuntimeException("clock error")
        return timeToReturn
    }

    override fun getZoneId(): ZoneId = zoneId
}
