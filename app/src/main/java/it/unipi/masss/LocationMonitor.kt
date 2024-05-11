package it.unipi.masss

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.provider.Settings
import java.util.concurrent.CompletableFuture


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
        notif = createNotification()
        startForeground(ProtectronApplication.BG_NOTIF_ID, notif)
        return START_NOT_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        handlerThread.quitSafely()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotification() : Notification {
        // create notification for when the service is started
        // use an intent to reopen the app if the notification is tapped
        val resultIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, ProtectronApplication.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Location Monitoring")
            .setContentText("Click to open the app")
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun getPreciseLocation(): Location? {
        val futureLocation = CompletableFuture<Location>()
        val fusLocClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1)
            .setMaxUpdateDelayMillis(1)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLoc = locationResult.lastLocation
                if(currentLoc != null) {
                    Log.d("PRECISE_LOC", currentLoc.toString())
                    fusedLocationClient.removeLocationUpdates(this)
                    futureLocation.complete(currentLoc)
                    return;
                }

            }
        }

        fusLocClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        return futureLocation.get()
    }

    /**Opens Google Maps and shows the position where the person in danger is located*/
    private fun openPersonInDangerLocation(lat: Double, long: Double) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?q=$lat,$long")
        )
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        startActivity(intent)
    }

}
