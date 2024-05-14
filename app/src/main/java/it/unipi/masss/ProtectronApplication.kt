package it.unipi.masss

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi

class ProtectronApplication : Application() {
    companion object {
        const val BG_NOTIF_ID = 69
        const val CHANNEL_ID = "PROTECTRON"
        const val CHANNEL_NAME = "Nearby danger finder" // TODO Change Name
        const val COUNTDOWN_S = 10
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        val sendAlertReceiver = SendAlertReceiver()
        val filter = IntentFilter()
        filter.addAction(Action.SEND_ALERT.toString())
        filter.addAction("ACTION_ABORT")
        registerReceiver(sendAlertReceiver, filter, RECEIVER_NOT_EXPORTED)
    }
}