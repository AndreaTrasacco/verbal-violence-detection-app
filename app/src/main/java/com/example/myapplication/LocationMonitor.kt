package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.provider.Settings

const val DELAY_MS: Long = 1000

class LocationMonitor : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastLoc: Location? = null
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var runnable: Runnable
    private lateinit var notif: Notification
    private lateinit var userID: String

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        userID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, DELAY_MS)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(DELAY_MS)
            .setMaxUpdateDelayMillis(DELAY_MS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLoc = locationResult.lastLocation
                if(currentLoc != null) {
                    lastLoc = currentLoc;
                    return;
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())

        //thread to communicate the last known location with server every DELAY_MS milliseconds
        handlerThread = HandlerThread("LocationUpdateThread")
        runnable = Runnable {
            if(lastLoc != null) {
                //TODO: SEND DATA TO SERVER
                Log.d("TEST", userID + " " + lastLoc.toString())
            }
            handler.postDelayed(runnable, DELAY_MS)
        }

        handlerThread.start()
        handler = Handler(handlerThread.looper)
        handler.post(runnable)

    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    // foreground service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotifChannel()
        startForeground(Util.BG_NOTIF_ID, notif)
        return START_NOT_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        handlerThread.quitSafely()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotifChannel() {
        // create notification channel
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(Util.CHANNEL_ID, Util.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        // create notification for when the service is started
        // use an intent to reopen the app if the notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        notif = NotificationCompat.Builder(this, Util.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(Util.APP_NAME)
            .setContentIntent(pendingIntent)
            .build()
    }

}
