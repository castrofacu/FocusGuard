package com.facucastro.focusguard.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import com.facucastro.focusguard.data.notification.FocusNotificationManager

internal class SessionNotificationController(
    private val service: Service,
    private val notificationManager: FocusNotificationManager,
) {

    private val systemNotificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    fun startForeground(elapsedSeconds: Int = 0, isPaused: Boolean = false) {
        val notification = notificationManager.buildSessionNotification(
            elapsedSeconds = elapsedSeconds,
            isPaused = isPaused,
            pauseIntent = buildPauseOrResumeIntent(isPaused),
            stopIntent = buildStopIntent(),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            service.startForeground(
                FocusNotificationManager.SESSION_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            service.startForeground(FocusNotificationManager.SESSION_NOTIFICATION_ID, notification)
        }
    }

    fun update(elapsedSeconds: Int, isPaused: Boolean) {
        val notification = notificationManager.buildSessionNotification(
            elapsedSeconds = elapsedSeconds,
            isPaused = isPaused,
            pauseIntent = buildPauseOrResumeIntent(isPaused),
            stopIntent = buildStopIntent(),
        )
        systemNotificationManager.notify(
            FocusNotificationManager.SESSION_NOTIFICATION_ID,
            notification,
        )
    }

    private fun buildPauseOrResumeIntent(isPaused: Boolean): PendingIntent {
        val action = if (isPaused) FocusSessionService.ACTION_RESUME else FocusSessionService.ACTION_PAUSE
        return PendingIntent.getService(
            service,
            REQUEST_CODE_PAUSE,
            Intent(service, FocusSessionService::class.java).apply { this.action = action },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun buildStopIntent(): PendingIntent = PendingIntent.getService(
        service,
        REQUEST_CODE_STOP,
        Intent(service, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_STOP
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val REQUEST_CODE_PAUSE = 10
        const val REQUEST_CODE_STOP = 11
    }
}
