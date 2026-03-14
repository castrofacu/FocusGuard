package com.facucastro.focusguard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.facucastro.focusguard.data.sync.SyncWorkScheduler
import com.facucastro.focusguard.notification.FocusNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FocusGuardApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationManager: FocusNotificationManager

    @Inject
    lateinit var syncWorkScheduler: SyncWorkScheduler

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannel()
        syncWorkScheduler.enqueueSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
