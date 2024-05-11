package it.unipi.masss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.unipi.masss.Util.isServiceRunning
import it.unipi.masss.recordingservice.RecordingService

class SendAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context?.isServiceRunning(RecordingService::class.java)!!){
            Intent(context.applicationContext, ShakingDetector::class.java).also {
                it.action = Action.STOP_SHAKING_DETECTION.toString()
                context.applicationContext?.startService(it)
            }
        }
        if(context.isServiceRunning(ShakingDetector::class.java)){
            Intent(context.applicationContext, RecordingService::class.java).also {
                it.action = Action.STOP_RECORDING.toString()
                context.applicationContext?.startService(it)
            }
        }

        // TODO Send Alert
    }
}