package it.unipi.masss.recordingservice

import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import it.unipi.masss.ml.AudioModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


object VerbalViolenceDetector {
    private const val TAG = "VerbalViolenceDetector"
    // function that executes the python script to get the features given the .wav file
    private fun extractFeatures(path: String): FloatArray {
        val py = Python.getInstance()
        val module = py.getModule("script")

        val funCall = module["get_features"]
        val result = funCall?.call(path)
        return result!!.toJava(FloatArray::class.java)
    }

    //function to classify the .wav given the array of features
    fun classify(recordingService: RecordingService, path: String): Boolean {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(recordingService))
        }
        val audioFeatures = extractFeatures(path)
        val model = AudioModel.newInstance(recordingService)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 162, 1), DataType.FLOAT32)
        inputFeature0.loadArray(audioFeatures)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        model.close()

        Log.d(TAG, "Detection result: " + outputFeature0.contentToString())

        val maxIdx = outputFeature0.indices.maxBy { outputFeature0[it] }

        return !((maxIdx == 0) || (maxIdx == 1) || (maxIdx == 2) || (maxIdx == 7))
    }

}