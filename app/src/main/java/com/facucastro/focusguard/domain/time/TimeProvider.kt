package com.facucastro.focusguard.domain.time

interface TimeProvider {
    fun getCurrentTimeMillis(): Long
}