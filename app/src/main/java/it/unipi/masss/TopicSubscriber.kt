package it.unipi.masss

import android.content.Context
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

object TopicSubscriber {
    fun subscribeToTopic(context: Context) {
        var sharedPreference = context.getSharedPreferences("SUBSCRIBED", Context.MODE_PRIVATE)
        val subscribed = sharedPreference.getBoolean("subscribed", false)

        if (!subscribed) {
            Firebase.messaging.subscribeToTopic("protectron").addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    Log.d(TopicSubscriber::class.java.simpleName, msg)
                }

            sharedPreference = context.getSharedPreferences("SUBSCRIBED", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putBoolean("subscribed", true)
            editor.apply()
        }
    }
}
