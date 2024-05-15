package it.unipi.masss

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import it.unipi.masss.Util.checkGenericPermission
import java.util.concurrent.CompletableFuture

object LocationHandling {

    fun getPreciseLocation(context: Context): CompletableFuture<Location?> {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val futureLocation = CompletableFuture<Location?>()
        val fusLocClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(1)
            .setMaxUpdateDelayMillis(1)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLoc = locationResult.lastLocation
                if(currentLoc != null) {
                    Log.d(TAG, "lat: ${currentLoc.latitude} long: ${currentLoc.longitude}")
                    fusedLocationClient.removeLocationUpdates(this)
                    futureLocation.complete(currentLoc)
                }
            }
        }

        if (checkGenericPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ||
            checkGenericPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusLocClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
        else {
            Log.d(TAG, "Can't fetch user location, permission denied")
            futureLocation.complete(null)
        }

        return futureLocation
    }

    val TAG = "LocationHandling"

}