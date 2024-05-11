package it.unipi.masss

import android.Manifest
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import it.unipi.masss.Util.checkGenericPermission
import it.unipi.masss.Util.isServiceRunning
import it.unipi.masss.recordingservice.RecordingService
import it.unipi.masss.ProtectronApplication.Companion.CHANNEL_ID
import it.unipi.masss.ProtectronApplication.Companion.COUNTDOWN_S
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SendAlertReceiver : BroadcastReceiver() {
    private var countDownTimer: CountDownTimer? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_ABORT") {
            countDownTimer?.cancel()
            if (context == null) return
            with(NotificationManagerCompat.from(context)) {
                cancel(1)
            }
        } else {
            if (context?.isServiceRunning(RecordingService::class.java)!!) {
                Intent(context.applicationContext, ShakingDetector::class.java).also {
                    it.action = Action.STOP_SHAKING_DETECTION.toString()
                    context.applicationContext?.startService(it)
                }
            }
            if (context.isServiceRunning(ShakingDetector::class.java)) {
                Intent(context.applicationContext, RecordingService::class.java).also {
                    it.action = Action.STOP_RECORDING.toString()
                    context.applicationContext?.startService(it)
                }
            }

            // intent to cancel count down
            val abortIntent = Intent(context, SendAlertReceiver::class.java)
            abortIntent.action = "ACTION_ABORT"
            val abortPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                abortIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // build danger alert notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setContentTitle(context.getString(R.string.danger_detected_notification))
                .setContentText(COUNTDOWN_S.toString() + context.getString(R.string.countdown))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(
                    R.drawable.ic_close,
                    context.getString(R.string.abort),
                    abortPendingIntent
                )

            // notify user
            if (checkGenericPermission(context, Manifest.permission.FOREGROUND_SERVICE)) {
                with(NotificationManagerCompat.from(context)) {
                    notify(1, builder.build())
                }
            }

            // Start countdown
            object : CountDownTimer(COUNTDOWN_S.toLong() * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    builder.setContentText("${millisUntilFinished / 1000}" + context.getString(R.string.countdown))

                    if (checkGenericPermission(context, Manifest.permission.FOREGROUND_SERVICE)) {
                        with(NotificationManagerCompat.from(context)) {
                            notify(1, builder.build())
                        }
                    }
                }

                override fun onFinish() {
                    // Execute function and remove notification
                    with(NotificationManagerCompat.from(context)) {
                        cancel(1)
                    }
                    // TODO: SEND ALERT
                }
            }.start()
        }
        // TODO Send Alert
        val apiUrl = "https://api.example.com/post" // TODO USE CONSTANT
        // TODO Get Location
        val postData = "key1=value1&key2=value2"
        val responseData = sendPostRequest(apiUrl, postData)
    }

    private fun sendPostRequest(urlString: String, postData: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val outputStream = connection.outputStream
        outputStream.use {
            val writer = OutputStreamWriter(it)
            writer.write(postData)
            writer.flush()
        }

        val response = StringBuilder()
        connection.inputStream.bufferedReader().use {
            it.lines().forEach { line ->
                response.append(line)
            }
        }

        return response.toString()
    }
}