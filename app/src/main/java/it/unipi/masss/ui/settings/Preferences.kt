package it.unipi.masss.ui.settings

import android.content.Context
import android.content.SharedPreferences
import it.unipi.masss.ProtectronApplication.Companion.SHARED_PREF

class SettingsPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun setAutoMonState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("AUTO_MON_STATE", state)
        editor.apply()
    }

    fun getAutoMonState(): Boolean {
        return sharedPreferences.getBoolean("AUTO_MON_STATE", false)
    }

    fun setCloseContactOptionState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("CLOSE_CONTACT_OPTION_STATE", state)
        editor.apply()
    }

    fun getCloseContactOptionState(): Boolean {
        return sharedPreferences.getBoolean("CLOSE_CONTACT_OPTION_STATE", false)
    }
}
