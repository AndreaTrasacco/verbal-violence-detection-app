package it.unipi.masss.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.unipi.masss.MainActivity
import it.unipi.masss.R

class OnAlertReceivedService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        // TODO metodo per recuperare il token
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // TODO Check che io non sia il mittente del messaggio
        Log.d(OnAlertReceivedService::class.java.simpleName, remoteMessage.toString())
        if (remoteMessage.notification != null) {
            // TODO come gestire la posizione in cui si verifica l'aggressione nella notifica??
            // Get location from message payload
            // Compute distance
            // If get distance < Threshold
            // Show notification which opens Maps if clicked (openPersonInDangerLocation)
            showNotification("A new violence is detected")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        // TODO archiviarlo usando firebase firestore? insieme alla posizione attuale?
        // TODO richiedere la posizione tramite un intent
        // val position = getPosition()
        if (token != null) {
            //FirebaseStoreManager.updatePosition(token, position)
        }
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun showNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alert!!")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    fun sendAlert(messageBody: String) {

    }

    companion object {
        private const val TAG = "FirebaseNotificationService"
    }
}