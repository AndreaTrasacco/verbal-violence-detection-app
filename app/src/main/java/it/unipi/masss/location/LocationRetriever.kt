package it.unipi.masss.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationServices
import it.unipi.masss.Util
import java.util.concurrent.CompletableFuture

object LocationRetriever {
    fun getPreciseLocation(context: Context): CompletableFuture<Location?> {
        val futureLocation = CompletableFuture<Location?>()

        // Start the service
        val intent = Intent(context, LocationService::class.java)
        context.startService(intent)

        // Wait for the service to finish and get the last known location
        CompletableFuture.runAsync {
            Thread.sleep(5000) // Wait for the service to get the location
            if (Util.checkGenericPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ||
                Util.checkGenericPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
                Util.checkGenericPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        futureLocation.complete(location)
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.d("DEBUG", "Error trying to get last GPS location")
                        futureLocation.complete(null)
                    }
            }
        }

        return futureLocation
    }
}
