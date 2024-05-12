package it.unipi.masss

import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.abs


class ShakingDetector : Service() {
    private lateinit var sensorEventListener: SensorEventListener
    private var numTimes : Int = 0
    private var lastUpdateOfNumTimes : Long = System.currentTimeMillis()

    companion object {
        const val SHAKING_DETECTIONS_THRESHOLD = 10
        const val MS_BETWEEN_NUM_TIMES : Long = 2000
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START_SHAKING_DETECTION.toString() -> startShakingDetection()
            Action.STOP_SHAKING_DETECTION.toString() -> stopShakingDetection()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startShakingDetection() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorShake = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                val xAccl = sensorEvent.values[0]
                val yAccl = sensorEvent.values[1]
                val zAccl = sensorEvent.values[2]

                val sum =
                    (abs(xAccl.toDouble()) + abs(yAccl.toDouble()) + abs(zAccl.toDouble())).toFloat()

                if (sum > 40) {
                    Log.d("ShakingDetector", "Detected shaking")
                    if(System.currentTimeMillis() - lastUpdateOfNumTimes < MS_BETWEEN_NUM_TIMES){
                        lastUpdateOfNumTimes = System.currentTimeMillis()
                        numTimes++
                    } else
                        numTimes = 0
                    if(numTimes >= SHAKING_DETECTIONS_THRESHOLD)
                    {
                        numTimes = 0;
                        sendBroadcast(Intent(Action.SEND_ALERT.toString()))
                    }
                }
                Log.d("ShakingDetector", "Sensor Changed $sum")
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {
            }
        }
        sensorManager.registerListener(
            sensorEventListener,
            sensorShake,
            SensorManager.SENSOR_DELAY_NORMAL // 215-230 ms
        )

        // TODO CODE REUSE
        // Create PendingIntent for starting MainActivity when notification is clicked
        val resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        // Create the persistent notification
        val notification = NotificationCompat.Builder(this, ProtectronApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Live Monitoring")
            .setContentText("Click to open the app")
            .setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true)
            .build()
        startForeground(ProtectronApplication.BG_NOTIF_ID, notification)
    }

    private fun stopShakingDetection(){
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(sensorEventListener)
        stopSelf();
    }
}