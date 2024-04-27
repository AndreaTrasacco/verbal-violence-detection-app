package it.unipi.masss.project
import android.app.Service
import android.content.Intent
import android.os.IBinder

class BackgroundMonitor : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPreferences = getSharedPreferences("BG_SERVICE", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isServiceRunning", true)
        editor.apply()
        // your code here
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        val sharedPreferences = getSharedPreferences("BG_SERVICE", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isServiceRunning", false)
        editor.apply()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
