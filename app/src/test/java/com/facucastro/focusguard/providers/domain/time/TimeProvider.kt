package com.facucastro.focusguard.providers.domain.time

import com.facucastro.focusguard.domain.time.TimeProvider
import java.time.ZoneId

fun providesFakeTimeProvider(
    timeToReturn: Long = 1000L,
    shouldThrow: Boolean = false,
    zoneToReturn: ZoneId = ZoneId.of("UTC")
) = object : TimeProvider {
    override fun getCurrentTimeMillis(): Long {
        if (shouldThrow) throw RuntimeException("clock error")
        return timeToReturn
    }

    override fun getZoneId(): ZoneId = zoneToReturn
}
