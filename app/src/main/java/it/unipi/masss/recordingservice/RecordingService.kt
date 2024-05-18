package it.unipi.masss.recordingservice

import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
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

/*
* Foreground service used for recording the sound from the environment, later used for classification
* as "violent" or "not violent".
* */
class RecordingService : Service() {
    private val timer: Timer = Timer()
    private val recorderTask: RecorderTask? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RecorderTask(this)
    } else null
    private lateinit var notificationBuilder: NotificationCompat.Builder

    companion object {
        const val CHECK_AMPLITUDE_PERIOD: Long = 20000 // ms
        private const val TAG = "RecordingService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var wakeLock: PowerManager.WakeLock
    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Protectron::MyWakelockTag"
        )
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld)
            wakeLock.release()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START_RECORDING.toString() -> startRecording()
            Action.STOP_RECORDING.toString() -> stopRecording()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /*
    * Method to schedule a periodic task which listens for "noise" from the environment. If "noise"
    * is detected, a recording is captured for classification purposes.
    * */
    @RequiresApi(Build.VERSION_CODES.R)
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
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(getString(R.string.live_mon_title))
            .setContentText(getString(R.string.click_to_open_app))
            .setContentIntent(resultPendingIntent)
            .setOnlyAlertOnce(true)
        startForeground(
            ProtectronApplication.BG_NOTIF_ID,
            notificationBuilder.build(),
            FOREGROUND_SERVICE_TYPE_MICROPHONE
        )
        // Start the recording logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            timer.schedule(RecorderTask(this), 0, CHECK_AMPLITUDE_PERIOD)
        }
    }

    /*
    * Method to stop the recording service, in case of violent detection a "SEND_ALERT" event is raised.
    * */
    fun stopRecording(alert: Boolean = false) {
        if (this.isServiceRunning(this::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorderTask?.cancel()
            }
            timer.cancel()
            if (alert) {
                Log.d(TAG, "Send alert!")
                sendBroadcast(Intent(Action.SEND_ALERT.toString()))
            }
            stopSelf() // Stop foreground service
        }
    }

    /*
    * Periodic task which implements the recording logic.
    * */
    @RequiresApi(Build.VERSION_CODES.S)
    class RecorderTask(private val recordingService: RecordingService) : TimerTask() {
        private var wavRecorder: WavRecorder? = null
        private var isCanceled = false
        private val handler = Handler(Looper.getMainLooper())

        companion object {
            const val AMPLITUDE_DB_THRESHOLD: Int = 55 // Decibels
            const val CHECK_AMPLITUDE_DURATION: Long = 2 // Seconds
            const val RECORDING_FOR_ML_SECONDS: Long = 10
            private const val TAG = "RecorderTask"
        }

        /*
        * The method stops the scheduling of the periodic task.
        * */
        override fun cancel(): Boolean {
            isCanceled = super.cancel()
            wavRecorder?.stopRecording()
            return isCanceled
        }

        /*
        * Method with the core logic of recording.
        * */
        override fun run() {
            // Initialization of mediaRecorder object
            if (wavRecorder == null)
                wavRecorder = WavRecorder(recordingService)
            wavRecorder?.startRecording("temp.wav", true)
            wavRecorder?.maxAmplitudeDb // Needed because in this way at the 2nd call we can get a value != -infinity

            Executors.newSingleThreadScheduledExecutor().schedule(
                {
                    handler.post {
                        val amplitude = wavRecorder?.maxAmplitudeDb!!
                        Log.d(TAG, "Detected amplitude: $amplitude dB")
                        wavRecorder?.stopRecording()
                        if (amplitude > AMPLITUDE_DB_THRESHOLD) {
                            Log.d(
                                TAG,
                                "Start recording for subsequent detection"
                            )
                            val outputFile = "recording_" + System.currentTimeMillis() + ".wav"
                            wavRecorder?.startRecording(outputFile, true)
                            Executors.newSingleThreadScheduledExecutor().schedule(
                                {
                                    handler.post {
                                        wavRecorder?.stopRecording()
                                        val violentRecording = VerbalViolenceDetector.classify(
                                            recordingService,
                                            recordingService.filesDir.path + '/' + outputFile
                                        )
                                        if (violentRecording && !isCanceled) {
                                            recordingService.stopRecording(true)
                                        } else {
                                            val fileDelete =
                                                File(recordingService.filesDir.path + '/' + outputFile)
                                            fileDelete.delete()
                                        }
                                    }
                                }, RECORDING_FOR_ML_SECONDS, TimeUnit.SECONDS
                            )
                        }
                    }
                }, CHECK_AMPLITUDE_DURATION, TimeUnit.SECONDS
            )
        }
    }
}