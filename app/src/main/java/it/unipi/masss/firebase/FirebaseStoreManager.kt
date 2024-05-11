package it.unipi.masss.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


object FirebaseStoreManager {
    private const val TAG = "FirebaseStoreManager"

    fun updatePosition(token: String, position: String) {
        val db = Firebase.firestore

        val map = hashMapOf(
            "token" to token,
            "position" to position
        )
        db.collection("position")
            .add(map)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun readNearby(position: String) {
        val db = Firebase.firestore
        // TODO funzione per la vicinanza, capire come viene salvata e come implementarla
        //
    }

}