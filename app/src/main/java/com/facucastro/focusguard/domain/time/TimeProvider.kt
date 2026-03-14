package com.facucastro.focusguard.domain.time

import java.time.ZoneId

interface TimeProvider {
    fun getCurrentTimeMillis(): Long
    fun getZoneId(): ZoneId
}