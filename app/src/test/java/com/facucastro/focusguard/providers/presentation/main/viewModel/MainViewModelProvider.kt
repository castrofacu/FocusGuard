package com.facucastro.focusguard.providers.presentation.main.viewModel

import com.facucastro.focusguard.domain.feature.FeatureFlagService
import com.facucastro.focusguard.domain.feature.FeatureFlags
import com.facucastro.focusguard.domain.usecase.ObserveLoginStateUseCase
import com.facucastro.focusguard.presentation.main.viewModel.MainViewModel
import com.facucastro.focusguard.providers.domain.feature.providesFakeFeatureFlagService
import com.facucastro.focusguard.providers.domain.repository.providesMockAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun providesMainViewModel(
    isUserLoggedIn: Flow<Boolean> = flowOf(false),
    featureFlagService: FeatureFlagService = providesFakeFeatureFlagService(),
): MainViewModel {
    val authRepository = providesMockAuthRepository(isUserLoggedIn = isUserLoggedIn)
    return MainViewModel(
        observeLoginStateUseCase = ObserveLoginStateUseCase(authRepository),
        featureFlagService = featureFlagService,
    )
}

fun providesMainViewModelWithLeaderboard(enabled: Boolean): MainViewModel =
    providesMainViewModel(
        featureFlagService = providesFakeFeatureFlagService(
            flags = mapOf(FeatureFlags.LEADERBOARD to enabled),
        ),
    )
