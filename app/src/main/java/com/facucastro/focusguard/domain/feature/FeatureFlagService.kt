package com.facucastro.focusguard.domain.feature

interface FeatureFlagService {
    suspend fun isEnabled(flag: String): Boolean
}
