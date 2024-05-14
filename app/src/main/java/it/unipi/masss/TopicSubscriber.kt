package it.unipi.masss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

/*
class TopicSubscriber : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED == intent.action ||
            Intent.ACTION_REBOOT == intent.action) {
            Firebase.messaging.subscribeToTopic("protectron")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    Log.d(TopicSubscriber::class.java.simpleName, msg)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }



        }
        if (Intent.ACTION_SHUTDOWN == intent.action)
            Firebase.messaging.unsubscribeFromTopic("protectron")
    }
}

 */
object TopicSubscriber {
    fun subscribeToTopic(context: Context) {
        val sharedPreference = context.getSharedPreferences("SUBSCRIBED", Context.MODE_PRIVATE)
        val subscribed = sharedPreference.getBoolean("subscribed", false)

        if (subscribed == false) {
            Firebase.messaging.subscribeToTopic("protectron")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    Log.d(TopicSubscriber::class.java.simpleName, msg)
                }

            val sharedPreference = context.getSharedPreferences("SUBSCRIBED", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putBoolean("subscribed", true)
            editor.apply()
        }
    }
}