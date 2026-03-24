package com.facucastro.focusguard.data.time

import com.facucastro.focusguard.domain.time.TimeProvider
import java.time.ZoneId
import javax.inject.Inject

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
    override fun getZoneId(): ZoneId = ZoneId.systemDefault()
}
