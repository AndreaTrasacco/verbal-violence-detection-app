package it.unipi.masss.recordingservice

import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import it.unipi.masss.Action
import it.unipi.masss.MainActivity
import it.unipi.masss.ProtectronApplication
import it.unipi.masss.R
import it.unipi.masss.Util.isServiceRunning
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class RecordingService : Service() {
    private val timer: Timer = Timer()
    private val recorderTask: RecorderTask? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RecorderTask(this)
    } else null
    private lateinit var notificationBuilder: NotificationCompat.Builder

    companion object {
        const val PERIOD: Long = 20000 // ms
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START_RECORDING.toString() -> startRecording()
            Action.STOP_RECORDING.toString() -> stopRecording()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
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
        notificationBuilder = NotificationCompat.Builder(this, ProtectronApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.live_mon_title))
            .setContentText(getString(R.string.click_to_open_app))
            .setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true)
        startForeground(ProtectronApplication.BG_NOTIF_ID, notificationBuilder.build())
        // Start the recording logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            timer.schedule(RecorderTask(this), 0, PERIOD)
        }
    }

    fun stopRecording(alert: Boolean = false) {
        if (this.isServiceRunning(this::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorderTask?.cancel()
            }
            timer.cancel()
            if (alert) {
                Log.d("DEBUG_RECORDING", "Sending alert")
                sendBroadcast(Intent(Action.SEND_ALERT.toString()))
            }
            stopSelf() // Stop foreground service
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    class RecorderTask(private val recordingService: RecordingService) : TimerTask() {
        private var wavRecorder: WavRecorder? = null
        private var isCanceled = false;

        companion object {
            const val AMPLITUDE_THRESHOLD: Int = 100
            const val CHECK_AMPLITUDE_SECONDS: Long = 2
            const val RECORDING_FOR_ML_SECONDS: Long = 10

        }

        override fun cancel(): Boolean {
            isCanceled = super.cancel()
            wavRecorder?.stopRecording()
            return isCanceled
        }

        override fun run() {
            // Initialization of mediaRecorder object
            if (wavRecorder == null)
                wavRecorder = WavRecorder(recordingService)
            wavRecorder?.startRecording("temp.wav", true)
            wavRecorder?.maxAmplitudeDb // Needed because in this way at the 2nd call we can get a value != -infinity

            Executors.newSingleThreadScheduledExecutor().schedule(
                {
                    val amplitude = wavRecorder?.maxAmplitudeDb!!
                    Log.d("DEBUG_RECORDING", "Detected amplitude: $amplitude dB")
                    wavRecorder?.stopRecording()
                    if (amplitude > AMPLITUDE_THRESHOLD) {
                        Log.d(
                            "DEBUG_RECORDING",
                            "Start recording for subsequent detection"
                        )
                        val outputFile = "recording_" + System.currentTimeMillis() + ".wav"
                        wavRecorder?.startRecording(outputFile, true)
                        Executors.newSingleThreadScheduledExecutor().schedule(
                            {
                                wavRecorder?.stopRecording()
                                val violentRecording = VerbalViolenceDetector.classify(
                                    recordingService,
                                    recordingService.filesDir.path + '/' + outputFile
                                )
                                if (violentRecording && !isCanceled) {
                                    recordingService.stopRecording(true)
                                }
                                else {
                                    val fileDelete =
                                        File(recordingService.filesDir.path + '/' + outputFile)
                                    fileDelete.delete()
                                }
                            }, RECORDING_FOR_ML_SECONDS, TimeUnit.SECONDS
                        )
                    }
                }, CHECK_AMPLITUDE_SECONDS, TimeUnit.SECONDS
            )
        }
    }
}