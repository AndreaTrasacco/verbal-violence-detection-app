package it.unipi.masss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OnBoot : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = Intent(context, LocationMonitor::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}