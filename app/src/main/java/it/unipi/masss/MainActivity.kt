package it.unipi.masss

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.unipi.masss.databinding.ActivityMainBinding

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

        TopicSubscriber.subscribeToTopic(this)
    }

    override fun onStart() {
        super.onStart()
        askRequiredPermissions()
    }

    /**returns false if any of the permissions passed as argument is not granted, true otherwise*/
    private fun checkRequiredPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**check if requested permissions are granted, if not create a popup and then ask for the grant*/
    private fun askRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.WAKE_LOCK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(1, Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }

        if (!checkRequiredPermission(permissions.toTypedArray())) {
            popUp(permissions.toTypedArray(), 1, R.string.perm_req_msg_1)
        }
        else if (!checkRequiredPermission(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
            popUp(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2, R.string.perm_req_msg_2)
        }
    }

    private fun askPermissions(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    /**shows alert dialog, if ok is pressed a set of permissions are requested otherwise the app is closed*/
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
            1 -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finish()
                }
                else {      // if each permission is granted proceed to ask the "always allow" choice for location
                    popUp(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2, R.string.perm_req_msg_2)
                }
            }
            2 -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    finish()
                }
            }
        }
    }

}