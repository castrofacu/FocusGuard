package com.facucastro.focusguard.doubles

import com.facucastro.focusguard.domain.time.TimeProvider

object TestDoubles {
    class FakeTimeProvider(
        var timeToReturn: Long = 1000L,
        var shouldThrow: Boolean = false
    ) : TimeProvider {
        override fun getCurrentTimeMillis(): Long {
            if (shouldThrow) throw RuntimeException("clock error")
            return timeToReturn
        }
    }
}
