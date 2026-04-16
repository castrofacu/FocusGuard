package com.facucastro.focusguard.data.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.facucastro.focusguard.data.notification.FocusNotificationManager
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.session.FocusSessionController
import com.facucastro.focusguard.domain.model.FocusSessionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "FocusSessionService"

@AndroidEntryPoint
class FocusSessionService : Service() {

    @Inject lateinit var controller: FocusSessionController
    @Inject lateinit var notificationManager: FocusNotificationManager

    private lateinit var notificationController: SessionNotificationController
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var hasStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationController = SessionNotificationController(this, notificationManager)

        controller.state
            .onEach { state -> onSessionStateChanged(state) }
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                hasStarted = true
                notificationController.startForeground()
                controller.start()
            }
            ACTION_PAUSE -> controller.pause()
            ACTION_RESUME -> controller.resume()
            ACTION_STOP -> controller.stop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "Service destroyed")
    }

    private fun onSessionStateChanged(state: FocusSessionState) {
        if (!hasStarted) return

        when (state.status) {
            SessionStatus.Running -> notificationController.update(state.elapsedSeconds, isPaused = false)
            SessionStatus.Paused  -> notificationController.update(state.elapsedSeconds, isPaused = true)
            SessionStatus.Idle    -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    companion object {
        const val ACTION_START  = "com.facucastro.focusguard.ACTION_START"
        const val ACTION_PAUSE  = "com.facucastro.focusguard.ACTION_PAUSE"
        const val ACTION_RESUME = "com.facucastro.focusguard.ACTION_RESUME"
        const val ACTION_STOP   = "com.facucastro.focusguard.ACTION_STOP"

        fun startIntent(context: Context)  =
            Intent(context, FocusSessionService::class.java).apply { action = ACTION_START }

        fun pauseIntent(context: Context)  =
            Intent(context, FocusSessionService::class.java).apply { action = ACTION_PAUSE }

        fun resumeIntent(context: Context) =
            Intent(context, FocusSessionService::class.java).apply { action = ACTION_RESUME }

        fun stopIntent(context: Context)   =
            Intent(context, FocusSessionService::class.java).apply { action = ACTION_STOP }
    }
}
