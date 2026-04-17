package com.facucastro.focusguard.data.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.facucastro.focusguard.MainActivity
import com.facucastro.focusguard.R
import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.notification.DistractionNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DistractionNotifier {

    companion object {
        const val DISTRACTION_CHANNEL_ID = "focus_distraction_channel"
        const val SESSION_CHANNEL_ID = "focus_session_channel"
        const val SESSION_NOTIFICATION_ID = 1000
        private const val DISTRACTION_NOTIFICATION_ID = 1001
    }

    fun createChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val distractionChannel = NotificationChannel(
            DISTRACTION_CHANNEL_ID,
            "Distraction Alerts",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifies when a distraction is detected during a focus session."
        }

        val sessionChannel = NotificationChannel(
            SESSION_CHANNEL_ID,
            "Active Focus Session",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Persistent notification shown while a focus session is running."
        }

        manager.createNotificationChannel(distractionChannel)
        manager.createNotificationChannel(sessionChannel)
    }

    fun buildSessionNotification(
        elapsedSeconds: Int,
        isPaused: Boolean,
        pauseIntent: PendingIntent,
        stopIntent: PendingIntent,
    ): Notification {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeFormatted = "%02d:%02d".format(minutes, seconds)

        val statusText = if (isPaused) "Paused · $timeFormatted" else "Focusing · $timeFormatted"
        val pauseLabel = if (isPaused) "Resume" else "Pause"

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(context, SESSION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("FocusGuard")
            .setContentText(statusText)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, pauseLabel, pauseIntent)
            .addAction(0, "Stop", stopIntent)
            .build()
    }

    override fun notifyDistraction(event: DistractionEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val text = when (event) {
            is DistractionEvent.Movement -> "Movement detected while focusing."
            is DistractionEvent.Noise -> "Noise detected while focusing."
        }

        val notification = NotificationCompat.Builder(context, DISTRACTION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Distraction Detected")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(DISTRACTION_NOTIFICATION_ID, notification)
    }
}
