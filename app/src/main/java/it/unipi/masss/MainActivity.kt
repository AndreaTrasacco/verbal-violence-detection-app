package it.unipi.masss

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import it.unipi.masss.databinding.ActivityMainBinding
import it.unipi.masss.Util.isServiceRunning
import it.unipi.masss.recordingservice.RecordingService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_recordings, R.id.navigation_home, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // start location monitor if not already started
        if (!(this.isServiceRunning(LocationMonitor::class.java))) {
            val intent = Intent(this, LocationMonitor::class.java)
            this.startService(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        checkLocationPermissions()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
                ),
                0
            )
            checkRecordingPermissions()
        }
    }

    /**check if fine and background location permissions are granted, ask to grant them if they are not*/
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            popUp(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1, R.string.welcome_msg_loc)
        } else if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            popUp(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                2,
                R.string.welcome_msg_loc
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun checkRecordingPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            popUp(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                ), 2, R.string.welcome_msg_mic
            )
        }
    }

    private fun askPermissions(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    /**shows alert dialog, if ok is pressed a permission is requested otherwise the app is closed*/
    private fun popUp(permissions: Array<String>, requestCode: Int, messageId: Int) {
        AlertDialog.Builder(this).setTitle(resources.getString(R.string.app_name))
            .setMessage(messageId).setPositiveButton("OK") { _, _ ->
                askPermissions(permissions, requestCode)
            }.setNegativeButton(R.string.close_app) { _, _ ->
                finish()
            }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1, 2 -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finish()
                } else {
                    checkLocationPermissions()
                }
            }
        }
    }


}