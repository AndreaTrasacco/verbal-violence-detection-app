package it.unipi.masss

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.CompletableFuture
import it.unipi.masss.Util.checkGenericPermission

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
                    Log.d("PRECISE_LOC", currentLoc.toString())
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
            Log.d("DEBUG", "Can't fetch user location, permission denied")
            futureLocation.complete(null)
        }

        return futureLocation
    }

    /**Opens Google Maps and shows the position where the person in danger is located*/
    fun openPersonInDangerLocation(context: Context, location: Location) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?q=${location.latitude},${location.longitude}")
        )
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        context.startActivity(intent)
    }

    fun distanceFromPersonInDanger(context: Context, personInDangerLocation: Location): Float {
        var retValue = Float.MAX_VALUE
        val userLoc = getPreciseLocation(context)

        // non blocking handling of location
        userLoc.thenAccept { location ->
            if (location == null) Log.d("DEBUG", "Couldn't fetch user location")
            else retValue = location.distanceTo(personInDangerLocation)
        }

        return retValue
    }

}