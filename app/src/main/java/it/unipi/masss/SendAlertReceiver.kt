package it.unipi.masss

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import it.unipi.masss.ProtectronApplication.Companion.CHANNEL_ID
import it.unipi.masss.ProtectronApplication.Companion.COUNTDOWN_S
import it.unipi.masss.ProtectronApplication.Companion.SHARED_PREF
import it.unipi.masss.Util.checkGenericPermission
import it.unipi.masss.Util.isServiceRunning
import it.unipi.masss.recordingservice.RecordingService
import it.unipi.masss.ui.settings.SettingsPreferences
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class SendAlertReceiver : BroadcastReceiver() {
    private var countDownTimer: CountDownTimer? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Action.ACTION_ABORT.toString()) {
            Log.d(TAG, "Action aborted, don't send the alert")
            countDownTimer?.cancel()
            if (context == null) return
            with(NotificationManagerCompat.from(context)) {
                cancel(1)
            }
        } else {
            if (context?.isServiceRunning(ShakingDetector::class.java)!!) {
                Intent(context.applicationContext, ShakingDetector::class.java).also {
                    it.action = Action.STOP_SHAKING_DETECTION.toString()
                    context.applicationContext?.startService(it)
                }
            }
            if (context.isServiceRunning(RecordingService::class.java)) {
                Intent(context.applicationContext, RecordingService::class.java).also {
                    it.action = Action.STOP_RECORDING.toString()
                    context.applicationContext?.startService(it)
                }
            }
            // intent to cancel count down
            val abortIntent = Intent(Action.ACTION_ABORT.toString())
            val abortPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                abortIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // build danger alert notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setContentTitle(context.getString(R.string.danger_detected_notification))
                .setContentText(COUNTDOWN_S.toString() + " " + context.getString(R.string.countdown))
                .setPriority(NotificationCompat.PRIORITY_HIGH).addAction(
                    R.drawable.ic_close, context.getString(R.string.abort), abortPendingIntent
                )

            // notify user
            if (checkGenericPermission(context, Manifest.permission.FOREGROUND_SERVICE)) {
                with(NotificationManagerCompat.from(context)) {
                    notify(1, builder.build())
                }
            }

            // Start countdown
            countDownTimer = AlertNotificationTimer(builder, context).start()
        }
    }

    class AlertNotificationTimer(
        private val builder: NotificationCompat.Builder, val context: Context
    ) : CountDownTimer(COUNTDOWN_S.toLong() * 1000, 1000) {

        private val apiUrl = "https://us-central1-protectronserver.cloudfunctions.net/alert"
        override fun onTick(millisUntilFinished: Long) {
            builder.setContentText("${millisUntilFinished / 1000} " + context.getString(R.string.countdown))

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

            LocationHandling.getPreciseLocation(context).thenApply { location ->
                if (location == null) {
                    Log.d(TAG, "Cannot fetch user precise location")
                    return@thenApply
                } else {
                    Log.d(this::class.java.simpleName, "Location fetched $location")
                    alertContacts(location)
                    if(SettingsPreferences(context).getCloseContactOptionState())
                        alertNearby(location)
                }
            }
        }

        private fun alertContacts(location : Location) {
            val sosMsg = "[Protectron Automatic Message] I am in danger! Help me! I am here: " +
                "http://maps.google.com/maps?q=${location.latitude},${location.longitude}"

            // Get all keys from SharedPreferences
            val sharedPreferences =
                context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            val allKeys = sharedPreferences.all.keys
            val contactKeys = allKeys.filter { it.startsWith("contact_info_") }

            // Iterate over each contact key
            for (key in contactKeys) {
                val contactInfo = sharedPreferences.getString(key, null)
                val (_, number) = contactInfo?.split(",") ?: continue
                sendSMS(context, number, sosMsg)
            }
        }

        private fun sendSMS(context: Context, phoneNumber: String, message: String) {
            val sentPI: PendingIntent = PendingIntent.getBroadcast(
                context, 0, Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null)
        }

        private fun alertNearby(location : Location) {
            val sharedPreference = context.getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            val token = sharedPreference.getString("token", "defaultValue")
            val postData =
                "token=" + token + "&lat=" + location.latitude + "&long=" + location.longitude
            sendPostRequest(apiUrl, postData)
        }

        private fun sendPostRequest(urlString: String, postData: String) {
            val okHttpClient = OkHttpClient()
            val requestBody = postData.toRequestBody("text/plain".toMediaType())
            val request = Request.Builder().post(requestBody).url(urlString).build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, "${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "Response: ${response.code}")
                }
            })
        }

        companion object {
            private const val TAG = "AlertNotificationTimer"
        }
    }

    companion object {
        private const val TAG = "SendAlertReceiver"
    }
}