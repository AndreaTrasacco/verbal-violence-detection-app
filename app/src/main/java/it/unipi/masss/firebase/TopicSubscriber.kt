package it.unipi.masss.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import it.unipi.masss.ProtectronApplication.Companion.SHARED_PREF

object TopicSubscriber {
    private const val TAG = "TopicSubscriber"
    fun subscribeToTopic(context: Context) {
        var sharedPreference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val subscribed = sharedPreference.getBoolean("subscribed", false)

        if (!subscribed) {
            Firebase.messaging.subscribeToTopic("protectron").addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    Log.d(TAG, msg)
                }

            sharedPreference = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putBoolean("subscribed", true)
            editor.apply()
        }
    }
}
