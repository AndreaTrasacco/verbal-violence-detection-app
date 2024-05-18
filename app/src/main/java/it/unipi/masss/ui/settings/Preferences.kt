package it.unipi.masss.ui.settings

import android.content.Context
import android.content.SharedPreferences

class SettingsPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)

    fun setCloseContactOptionState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("CLOSE_CONTACT_OPTION_STATE", state)
        editor.apply()
    }

    fun getCloseContactOptionState(): Boolean {
        return sharedPreferences.getBoolean("CLOSE_CONTACT_OPTION_STATE", false)
    }
}
