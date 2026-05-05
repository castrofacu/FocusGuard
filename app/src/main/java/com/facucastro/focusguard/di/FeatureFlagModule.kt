package com.facucastro.focusguard.di

import com.facucastro.focusguard.BuildConfig
import com.facucastro.focusguard.data.feature.RemoteConfigFeatureFlagService
import com.facucastro.focusguard.domain.feature.FeatureFlagService
import com.facucastro.focusguard.domain.feature.FeatureFlags
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureFlagModule {

    @Binds
    @Singleton
    abstract fun bindFeatureFlagService(
        impl: RemoteConfigFeatureFlagService,
    ): FeatureFlagService

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
            val fetchIntervalSeconds = if (BuildConfig.DEBUG) 0L else 3600L
            return FirebaseRemoteConfig.getInstance().apply {
                setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = fetchIntervalSeconds
                    }
                )
                setDefaultsAsync(
                    mapOf(FeatureFlags.LEADERBOARD to false)
                )
            }
        }
    }
}
