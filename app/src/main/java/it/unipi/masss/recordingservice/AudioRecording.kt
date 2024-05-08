package it.unipi.masss.recordingservice

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
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

    companion object {
        const val PERIOD: Long = 20000 // ms
    }

    fun startAudioRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            timer.schedule(RecorderTask(recordingService), 0, PERIOD)
        }
    }

    fun stopAudioRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorderTask?.cancel()
        }
        timer.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    class RecorderTask(private val recordingService: RecordingService) : TimerTask() {
        private var wavRecorder: WavRecorder? = null

        companion object {
            const val AMPLITUDE_THRESHOLD: Int = 60
            const val CHECK_AMPLITUDE_SECONDS: Long = 2
            const val RECORDING_FOR_ML_SECONDS: Long = 10

        }

        override fun cancel(): Boolean {
            val retValue = super.cancel()
            wavRecorder?.stopRecording()
            return retValue
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
                    Log.d(RecorderTask::class.java.name, "Detected amplitude: $amplitude dB")
                    wavRecorder?.stopRecording()
                    if (amplitude > AMPLITUDE_THRESHOLD) {
                        Log.d(RecorderTask::class.java.name, "Start recording for subsequent detection")
                        val outputFile = "recording_" + System.currentTimeMillis() + ".wav"
                        wavRecorder?.startRecording(outputFile, true)
                        Executors.newSingleThreadScheduledExecutor().schedule(
                            {
                                wavRecorder?.stopRecording()
                                val violentRecording = AudioModelManager.classify(
                                    recordingService,
                                    recordingService.filesDir.path + '/' + outputFile
                                )
                                if (violentRecording) // TODO Is there the need of copying the audio?
                                    recordingService.stopRecording(true)
                                else {
                                    val fileDelete =
                                        File(recordingService.filesDir.path + '/' + outputFile)
                                    fileDelete.delete() // TODO Check against IOException
                                }
                            }, RECORDING_FOR_ML_SECONDS, TimeUnit.SECONDS
                        )
                    }
                }, CHECK_AMPLITUDE_SECONDS, TimeUnit.SECONDS
            )
        }
    }
}