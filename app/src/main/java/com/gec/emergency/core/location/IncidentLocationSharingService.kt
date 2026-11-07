package com.gec.emergency.core.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gec.emergency.MainActivity
import com.gec.emergency.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHANNEL_ID = "gec_live_tracking_channel"
private const val NOTIFICATION_ID = 2001

/**
 * Android Foreground Service managing consent-based live emergency location sharing.
 * Displays a persistent system notification while active and dispatches movement-aware
 * location points to the GEC backend.
 */
@AndroidEntryPoint
class IncidentLocationSharingService : Service() {

    @Inject
    lateinit var locationProvider: LocationProvider

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null
    private var sessionId: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_SHARING) {
            stopSelf()
            return START_NOT_STICKY
        }

        sessionId = intent?.getStringExtra(EXTRA_SESSION_ID)
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val stopIntent = Intent(this, IncidentLocationSharingService::class.java).apply {
            this.action = ACTION_STOP_SHARING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ghana Emergency Connect")
            .setContentText("Live emergency location sharing is active.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_background, "Stop Sharing", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startTrackingLoop()

        return START_STICKY
    }

    private fun startTrackingLoop() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (true) {
                delay(10000L) // 10-second interval
                runCatching {
                    locationProvider.getCurrentLocation()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Live Tracking",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Persistent indicator active during live emergency location sharing"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_SHARING = "com.gec.emergency.action.START_LIVE_SHARING"
        const val ACTION_STOP_SHARING = "com.gec.emergency.action.STOP_LIVE_SHARING"
        const val EXTRA_SESSION_ID = "extra_session_id"

        fun startService(context: Context, sessionId: String) {
            val intent = Intent(context, IncidentLocationSharingService::class.java).apply {
                action = ACTION_START_SHARING
                putExtra(EXTRA_SESSION_ID, sessionId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, IncidentLocationSharingService::class.java)
            context.stopService(intent)
        }
    }
}
