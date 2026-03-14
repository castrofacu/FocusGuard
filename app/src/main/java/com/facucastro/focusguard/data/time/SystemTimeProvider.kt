package com.facucastro.focusguard.data.time

import com.facucastro.focusguard.domain.time.TimeProvider
import javax.inject.Inject

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
}
