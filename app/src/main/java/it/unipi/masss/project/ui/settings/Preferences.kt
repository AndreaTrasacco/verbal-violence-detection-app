package it.unipi.masss.project.ui.settings

import android.content.Context
import android.content.SharedPreferences

class SettingsPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)

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

    fun setChosenContactOptionState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("SPECIFIC_CONTACT_OPTION_STATE", state)
        editor.apply()
    }

    fun getChosenContactOptionState(): Boolean {
        return sharedPreferences.getBoolean("SPECIFIC_CONTACT_OPTION_STATE", false)
    }
}
