package it.unipi.masss

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class ProtectronApplication : Application() {
    companion object NotificationInfo {
        const val BG_NOTIF_ID = 69
        const val CHANNEL_ID = "PROTECTRON"
        const val CHANNEL_NAME = "Nearby danger finder"
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}