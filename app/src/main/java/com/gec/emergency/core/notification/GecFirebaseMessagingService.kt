package com.gec.emergency.core.notification

import android.content.Context

/**
 * Receiver helper for handling status update notification events in Android.
 */
class GecFirebaseMessagingService {
    fun handleNotificationEvent(context: Context, title: String, message: String) {
        NotificationHelper.showReportUpdateNotification(context, title, message)
    }
}
