package it.unipi.masss

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object Util {
    fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = manager.runningAppProcesses
        return runningProcesses.any { it.processName == service.name }
    }

    /**Returns true if the user has granted the permission, false otherwise*/
    fun checkGenericPermission(context: Context, permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
}