package com.facucastro.focusguard.domain.time

class FakeTimeProvider(
    var timeToReturn: Long = 1000L,
    var shouldThrow: Boolean = false
) : TimeProvider {
    override fun getCurrentTimeMillis(): Long {
        if (shouldThrow) throw RuntimeException("clock error")
        return timeToReturn
    }
}
