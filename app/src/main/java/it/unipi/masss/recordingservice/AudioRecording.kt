package it.unipi.masss.recordingservice

import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.log10


class AudioRecording(private val recordingService: RecordingService) {
    private val timer: Timer = Timer()
    private val recorderTask: RecorderTask? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RecorderTask(recordingService)
    } else null
    private val PERIOD: Long = 20000 // ms

    fun startAudioRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            timer.schedule(RecorderTask(recordingService), 0, PERIOD)
        }
    }

    fun stopAudioRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorderTask?.cancel()
        }
        timer.cancel();
    }

    @RequiresApi(Build.VERSION_CODES.S)
    class RecorderTask(private val recordingService: RecordingService) : TimerTask() {
        private var mediaRecorder: MediaRecorder? = null
        private val AMPLITUDE_THRESHOLD: Double = 60.0
        private val CHECK_AMPLITUDE_SECONDS: Long = 1
        private val RECORDING_FOR_ML_SECONDS: Long = 10

        override fun cancel(): Boolean {
            val retValue = super.cancel()
            mediaRecorder?.release()
            mediaRecorder = null
            return retValue
        }

        override fun run() {
            // Initialization of mediaRecorder object
            if (mediaRecorder == null)
                mediaRecorder = MediaRecorder(recordingService)
            startMediaRecorder(recordingService.filesDir.absolutePath + "/temp.wav")
            getAmplitudeDB(mediaRecorder!!) // Needed because in this way at the 2nd call we can get a value != -infty

            Executors.newSingleThreadScheduledExecutor().schedule(
                {
                    var amplitude = 0.0
                    mediaRecorder?.apply {
                        stop()
                        amplitude = getAmplitudeDB(this)
                        Log.d("RecordingTask", "Detected amplitude: $amplitude dB")
                        reset()
                    }
                    if (amplitude > AMPLITUDE_THRESHOLD) {
                        Log.d("RecordingTask", "Start recording for subsequent detection")
                        val outputFile =
                            recordingService.filesDir.absolutePath + "/recording" + getTimestamp() + ".wav"
                        startMediaRecorder(outputFile)
                        Executors.newSingleThreadScheduledExecutor().schedule(
                            {
                                mediaRecorder?.apply {
                                    stop()
                                    reset()
                                }
                                // TODO CALL ML model with $outputFile, Get result
                                val violentRecording = false
                                if (violentRecording) // TODO Is there the need of copying the audio?
                                    recordingService.stopRecording(true)
                                else{
                                    val fdelete = File(outputFile)
                                    fdelete.delete()
                                }
                            }, RECORDING_FOR_ML_SECONDS, TimeUnit.SECONDS
                        )
                    }
                }, CHECK_AMPLITUDE_SECONDS, TimeUnit.SECONDS
            )
        }

        private fun startMediaRecorder(outputFile: String) {
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // wav format
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)

                try {
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                start()
            }
        }

        private fun getAmplitudeDB(mediaRecorder: MediaRecorder): Double =
            20 * log10(abs(mediaRecorder.maxAmplitude.toDouble()))

        private fun getTimestamp(): String = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(
            Date()
        )
    }
}