package it.unipi.masss

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class ProtectronApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            Util.CHANNEL_ID,
            Util.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}