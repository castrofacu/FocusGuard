package com.facucastro.focusguard.data.feature

import android.util.Log
import com.facucastro.focusguard.domain.feature.FeatureFlagService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RemoteConfigFeatureFlagService"

@Singleton
class RemoteConfigFeatureFlagService @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) : FeatureFlagService {

    override suspend fun isEnabled(flag: String): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
            remoteConfig.getBoolean(flag)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote config flag '$flag', defaulting to false", e)
            false
        }
    }
}
