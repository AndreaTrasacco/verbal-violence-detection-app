package it.unipi.masss.recordingservice

import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import it.unipi.masss.ml.AudioModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


object AudioModelManager{
    var model : AudioModel? = null
    private fun extractFeatures(path:String): FloatArray {
        Log.d(
            "AUDIO MODEL MANAGER",
            "before getInstance"
        )
        val py = Python.getInstance()
        Log.d(
            "AUDIO MODEL MANAGER",
            "after getInstance"
        )
        val module = py.getModule("script")
        Log.d(
            "AUDIO MODEL MANAGER",
            "after getModule"
        )

        val funCall = module["get_features"]
        Log.d(
            "AUDIO MODEL MANAGER",
            "after module[\"get_features\"] " + funCall.toString()
        )
        val result = funCall?.call(path)
        Log.d(
            "AUDIO MODEL MANAGER",
            "after call"
        )
        return result!!.toJava(FloatArray::class.java)
    }
    fun classify(recordingService: RecordingService, path: String): Boolean {
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(recordingService));
        }
        Log.d(
            "AUDIO MODEL MANAGER",
            "before extract feature"
        )
        val audioFeatures = extractFeatures(path)
        Log.d(
            "AUDIO MODEL MANAGER",
            "after extract feature"
        )
        //TODO cambiare logica di caricamento del modello
        if (model == null)
            model = AudioModel.newInstance(recordingService)

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 162, 1), DataType.FLOAT32)
        inputFeature0.loadArray(audioFeatures)

        // Runs model inference and gets result.
        val outputs = model!!.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        Log.d(
            "AUDIO MODEL MANAGER",
            "feature string: " + outputFeature0.contentToString()
        )

        val maxIdx = outputFeature0.indices.maxBy { outputFeature0[it] } ?: -1

        return !((maxIdx == 0) || (maxIdx == 1) || (maxIdx == 2) || (maxIdx == 7))
    }

    fun destroyModel() {
        // Releases model resources if no longer used.
        model?.close()
        model = null
    }
}