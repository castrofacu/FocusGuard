package com.facucastro.focusguard.providers.domain.feature

import com.facucastro.focusguard.domain.feature.FeatureFlagService

class FakeFeatureFlagService(
    private val flags: Map<String, Boolean> = emptyMap(),
) : FeatureFlagService {
    override suspend fun isEnabled(flag: String): Boolean = flags[flag] ?: false
}

fun providesFakeFeatureFlagService(
    flags: Map<String, Boolean> = emptyMap(),
): FeatureFlagService = FakeFeatureFlagService(flags)
