package it.unipi.masss.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import it.unipi.masss.it.unipi.masss.location.LocationHandling
import it.unipi.masss.ProtectronApplication.Companion.CHANNEL_NAME
import it.unipi.masss.R
import java.util.concurrent.CompletableFuture

class LocationService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_NAME)
            .setContentTitle("Location Service")
            .setContentText("Getting location...")
            .setSmallIcon(R.drawable.ic_home_black_24dp)
            .build()

        startForeground(1, notification)

        CompletableFuture.runAsync {
            LocationHandling.getPreciseLocation(applicationContext).get()
            stopSelf()
        }

        return START_NOT_STICKY
    }

}