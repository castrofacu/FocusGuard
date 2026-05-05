package com.facucastro.focusguard.data.feature

import android.util.Log
import com.facucastro.focusguard.domain.feature.FeatureFlagService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RemoteConfigFeatureFlagService"

@Singleton
class RemoteConfigFeatureFlagService @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) : FeatureFlagService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            try {
                remoteConfig.fetchAndActivate().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Remote Config fetch failed, cached/default values will be used", e)
            }
        }
    }

    override suspend fun isEnabled(flag: String): Boolean {
        return remoteConfig.getBoolean(flag)
    }
}
