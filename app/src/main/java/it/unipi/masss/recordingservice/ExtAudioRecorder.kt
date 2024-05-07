// Code adapted from https://web.archive.org/web/20150626112354/http://i-liger.com/article/android-wav-audio-recording
package it.unipi.masss.recordingservice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.OnRecordPositionUpdateListener
import android.media.MediaRecorder.AudioSource
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class ExtAudioRecorder @SuppressLint("MissingPermission") constructor(
    audioSource: Int,
    sampleRate: Int,
    channelConfig: Int,
    audioFormat: Int
) {
    /**
     * INITIALIZING : recorder is initializing;
     * READY : recorder has been initialized, recorder not yet started
     * RECORDING : recording
     * ERROR : reconstruction needed
     * STOPPED: reset needed
     */
    enum class State {
        INITIALIZING, READY, RECORDING, ERROR, STOPPED
    }

    // Recorder used for uncompressed recording
    private var audioRecorder: AudioRecord? = null

    // Stores current amplitude
    private var cAmplitude = 0

    // Output file path
    private var filePath: String? = null

    /**
     * Returns the state of the recorder in a RehearsalAudioRecord.State typed object.
     * Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    // Recorder state; see State
    private var state: State? = null

    // File writer
    private var randomAccessWriter: RandomAccessFile? = null

    // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
    private var nChannels: Short = 0
    private var sRate = 0
    private var bSamples: Short = 0
    private var bufferSize = 0
    private var aSource = 0
    private var aFormat = 0

    // Number of frames written to file on each output
    private var framePeriod = 0

    // Buffer for output
    private lateinit var buffer: ByteArray

    // Number of bytes written to file after header
    // after stop() is called, this size is written to the header/data chunk in the wave file
    private var payloadSize = 0

    /*
     *
     * Method used for recording.
     *
     */
    private val updateListener: OnRecordPositionUpdateListener =
        object : OnRecordPositionUpdateListener {
            override fun onPeriodicNotification(recorder: AudioRecord) {
                audioRecorder!!.read(buffer, 0, buffer.size) // Fill buffer
                try {
                    randomAccessWriter!!.write(buffer) // Write buffer to file
                    payloadSize += buffer.size
                    if (bSamples.toInt() == 16) {
                        for (i in 0 until buffer.size / 2) { // 16bit sample size
                            val curSample = getShort(buffer[i * 2], buffer[i * 2 + 1])
                            if (curSample > cAmplitude) { // Check amplitude
                                cAmplitude = curSample.toInt()
                            }
                        }
                    } else { // 8bit sample size
                        for (b in buffer) {
                            if (b > cAmplitude) { // Check amplitude
                                cAmplitude = b.toInt()
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e(
                        ExtAudioRecorder::class.java.name,
                        "Error occurred in updateListener, recording is aborted"
                    )
                    //stop();
                }
            }

            override fun onMarkerReached(recorder: AudioRecord) {
                // NOT USED
            }
        }

    /**
     * Default constructor
     * Instantiates a new recorder
     * In case of errors, no exception is thrown, but the state is set to ERROR
     */
    init {
        try {
            bSamples = if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                16
            } else {
                8
            }

            nChannels = if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
                1
            } else {
                2
            }

            aSource = audioSource
            sRate = sampleRate
            aFormat = audioFormat

            framePeriod = sampleRate * TIMER_INTERVAL / 1000
            bufferSize = framePeriod * 2 * bSamples * nChannels / 8
            if (bufferSize < AudioRecord.getMinBufferSize(
                    sampleRate,
                    channelConfig,
                    audioFormat
                )
            ) { // Check to make sure buffer size is not smaller than the smallest allowed one
                bufferSize =
                    AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                // Set frame period and timer interval accordingly
                framePeriod = bufferSize / (2 * bSamples * nChannels / 8)
                Log.w(
                    ExtAudioRecorder::class.java.name,
                    "Increasing buffer size to $bufferSize"
                )
            }

            audioRecorder =
                AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)

            if (audioRecorder!!.state != AudioRecord.STATE_INITIALIZED) throw Exception("AudioRecord initialization failed")
            audioRecorder!!.setRecordPositionUpdateListener(updateListener)
            audioRecorder!!.setPositionNotificationPeriod(framePeriod)
            cAmplitude = 0
            filePath = null
            state = State.INITIALIZING
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            } else {
                Log.e(
                    ExtAudioRecorder::class.java.name,
                    "Unknown error occurred while initializing recording"
                )
            }
            state = State.ERROR
        }
    }

    /**
     * Sets output file path, call directly after construction/reset.
     *
     * @param argPath output file path
     */
    fun setOutputFile(argPath: String?) {
        try {
            if (state == State.INITIALIZING) {
                filePath = argPath
            }
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            } else {
                Log.e(
                    ExtAudioRecorder::class.java.name,
                    "Unknown error occurred while setting output path"
                )
            }
            state = State.ERROR
        }
    }

    val maxAmplitude: Int
        /**
         * Returns the largest amplitude sampled since the last call to this method.
         *
         * @return returns the largest amplitude since the last call, or 0 when not in recording state.
         */
        get() {
            if (state == State.RECORDING) {
                val result = cAmplitude
                cAmplitude = 0
                return result
            } else {
                return 0
            }
        }


    /**
     * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
     * the recorder is set to the ERROR state, which makes a reconstruction necessary.
     * The header of the wave file is written.
     * In case of an exception, the state is changed to ERROR
     */
    fun prepare() {
        try {
            if (state == State.INITIALIZING) {
                if ((audioRecorder!!.state == AudioRecord.STATE_INITIALIZED) and (filePath != null)) {
                    // write file header
                    randomAccessWriter = RandomAccessFile(filePath, "rw")
                    randomAccessWriter!!.setLength(0) // Set file length to 0, to prevent unexpected behavior in case the file already existed
                    randomAccessWriter!!.writeBytes("RIFF")
                    randomAccessWriter!!.writeInt(0) // Final file size not known yet, write 0
                    randomAccessWriter!!.writeBytes("WAVE")
                    randomAccessWriter!!.writeBytes("fmt ")
                    randomAccessWriter!!.writeInt(Integer.reverseBytes(16)) // Sub-chunk size, 16 for PCM
                    randomAccessWriter!!.writeShort(
                        java.lang.Short.reverseBytes(1.toShort()).toInt()
                    ) // AudioFormat, 1 for PCM
                    randomAccessWriter!!.writeShort(
                        java.lang.Short.reverseBytes(nChannels).toInt()
                    ) // Number of channels, 1 for mono, 2 for stereo
                    randomAccessWriter!!.writeInt(Integer.reverseBytes(sRate)) // Sample rate
                    randomAccessWriter!!.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)) // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                    randomAccessWriter!!.writeShort(
                        java.lang.Short.reverseBytes((nChannels * bSamples / 8).toShort())
                            .toInt()
                    ) // Block align, NumberOfChannels*BitsPerSample/8
                    randomAccessWriter!!.writeShort(
                        java.lang.Short.reverseBytes(bSamples).toInt()
                    ) // Bits per sample
                    randomAccessWriter!!.writeBytes("data")
                    randomAccessWriter!!.writeInt(0) // Data chunk size not known yet, write 0

                    buffer = ByteArray(framePeriod * bSamples / 8 * nChannels)
                    state = State.READY
                } else {
                    Log.e(
                        ExtAudioRecorder::class.java.name,
                        "prepare() method called on uninitialized recorder"
                    )
                    state = State.ERROR
                }
            } else {
                Log.e(ExtAudioRecorder::class.java.name, "prepare() method called on illegal state")
                release()
                state = State.ERROR
            }
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            } else {
                Log.e(ExtAudioRecorder::class.java.name, "Unknown error occurred in prepare()")
            }
            state = State.ERROR
        }
    }

    /**
     * Releases the resources associated with this class, and removes the unnecessary files, when necessary
     */
    fun release() {
        if (state == State.RECORDING) {
            stop()
        } else {
            if (state == State.READY) {
                try {
                    randomAccessWriter!!.close() // Remove prepared file
                } catch (e: IOException) {
                    Log.e(
                        ExtAudioRecorder::class.java.name,
                        "I/O exception occurred while closing output file"
                    )
                }
                filePath?.let { File(it).delete() }
            }
        }

        if (audioRecorder != null) {
            audioRecorder!!.release()
        }
    }

    /**
     * Resets the recorder to the INITIALIZING state, as if it was just created.
     * In case the class was in RECORDING state, the recording is stopped.
     * In case of exceptions the class is set to the ERROR state.
     */
    @SuppressLint("MissingPermission")
    fun reset() {
        try {
            if (state != State.ERROR) {
                release()
                filePath = null // Reset file path
                cAmplitude = 0 // Reset amplitude
                audioRecorder = AudioRecord(aSource, sRate, AudioFormat.CHANNEL_IN_MONO, aFormat, bufferSize)
                state = State.INITIALIZING
            }
        } catch (e: Exception) {
            Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            state = State.ERROR
        }
    }

    /**
     * Starts the recording, and sets the state to RECORDING.
     * Call after prepare().
     */
    fun start() {
        if (state == State.READY) {
            payloadSize = 0
            audioRecorder!!.startRecording()
            audioRecorder!!.read(buffer, 0, buffer.size)
            state = State.RECORDING
        } else {
            Log.e(ExtAudioRecorder::class.java.name, "start() called on illegal state")
            state = State.ERROR
        }
    }

    /**
     * Stops the recording, and sets the state to STOPPED.
     * In case of further usage, a reset is needed.
     * Also finalizes the wave file.
     */
    fun stop() {
        if (state == State.RECORDING) {
            audioRecorder!!.stop()

            try {
                randomAccessWriter!!.seek(4) // Write size to RIFF header
                randomAccessWriter!!.writeInt(Integer.reverseBytes(36 + payloadSize))

                randomAccessWriter!!.seek(40) // Write size to Sub chunk2Size field
                randomAccessWriter!!.writeInt(Integer.reverseBytes(payloadSize))

                randomAccessWriter!!.close()
            } catch (e: IOException) {
                Log.e(
                    ExtAudioRecorder::class.java.name,
                    "I/O exception occurred while closing output file"
                )
                state = State.ERROR
            }
            state = State.STOPPED
        } else {
            Log.e(ExtAudioRecorder::class.java.name, "stop() called on illegal state")
            state = State.ERROR
        }
    }

    /*
     *
     * Converts a byte[2] to a short, in LITTLE_ENDIAN format
     *
     */
    private fun getShort(argB1: Byte, argB2: Byte): Short {
        return (argB1.toInt() or (argB2.toInt() shl 8)).toShort()
    }

    companion object {
        private const val SAMPLE_RATE = 44100

        fun getInstance(): ExtAudioRecorder {
            return ExtAudioRecorder(
                AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
        }

        // The interval in which the recorded samples are output to the file
        private const val TIMER_INTERVAL = 120
    }
}