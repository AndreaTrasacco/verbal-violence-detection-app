package it.unipi.masss.recordingservice

import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import it.unipi.masss.R
import it.unipi.masss.MainActivity
import it.unipi.masss.ProtectronApplication

class RecordingService : Service() {
    private val audioRecordingTask = AudioRecording(this)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START.toString() -> startRecording()
            Action.STOP.toString() -> stopRecording()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        AudioModelManager.destroyModel()
        super.onDestroy()
    }

    private fun startRecording() {
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
            .setContentTitle("Live Recording")
            .setContentText("Click to open the app")
            .setContentIntent(resultPendingIntent)
            .build()
        startForeground(ProtectronApplication.BG_NOTIF_ID, notification)
        // Start the recording logic
        audioRecordingTask.startAudioRecording();
    }

    fun stopRecording(alert : Boolean = false){
        audioRecordingTask.stopAudioRecording()
        if (alert){
            Log.d("RecordingService", "Send alert!")
            // TODO SEND ALERT (RECORDING BUTTON MUST BE DISABLED)
        }
        stopSelf() // Stop foreground service
    }

    enum class Action {
        START, STOP
    }
}