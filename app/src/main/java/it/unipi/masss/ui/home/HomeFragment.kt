package it.unipi.masss.ui.home

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.unipi.masss.LocationMonitor
import com.google.android.material.button.MaterialButton
import it.unipi.masss.project.R
import it.unipi.masss.project.databinding.FragmentHomeBinding
import android.telephony.SmsManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.app.PendingIntent


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    @Suppress("DEPRECATION")
    fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Integer.MAX_VALUE)
            .any { it -> it.service.className == service.name }
    }


    private fun sendSMS(context: Context, phoneNumber: String, message: String) {
        val sentPI: PendingIntent = PendingIntent.getBroadcast(context, 0, Intent("SMS_SENT"),
            PendingIntent.FLAG_IMMUTABLE)
        val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val start_mon_btn = view.findViewById<MaterialButton>(R.id.start_mon_btn)
        val manual_sos_btn = view.findViewById<MaterialButton>(R.id.manual_sos_btn)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Example", "Permission is granted")
                } else {
                    Log.i("Example", "Permission not granted")
                }
            }

        val hasPermission = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.SEND_SMS
        )== PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }

        requireActivity().getSystemService(SmsManager::class.java)
        val phoneNumber = "1234567890" // Replace with the phone number you want to send the SMS to
        val message = "Hello, this is a test message!" // Replace with your message

        start_mon_btn.setOnClickListener {

            // detect if the service is already running
            var isActive = requireContext().isServiceRunning(LocationMonitor::class.java)

            if (!isActive) {
                // start background monitoring service
                val intent = Intent(context, LocationMonitor::class.java)
                context?.startService(intent)

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
                start_mon_btn.backgroundTintList = colorStateList
            }
            else {
                // stop background monitoring service
                val intent = Intent(context, LocationMonitor::class.java)
                context?.stopService(intent)
                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
                start_mon_btn.backgroundTintList = colorStateList
            }
        }

        manual_sos_btn.setOnClickListener {
            // Get all keys from SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val allKeys = sharedPreferences.all.keys

            // Filter the keys that start with "contact_info_"
            val contactKeys = allKeys.filter { it.startsWith("contact_info_") }

            // Iterate over each contact key
            for (key in contactKeys) {
                // Get the contact info from SharedPreferences
                val contactInfo = sharedPreferences.getString(key, null)

                // Split the contact info into name and number
                val (name, number) = contactInfo?.split(",") ?: continue

                // Call the sendSMS function
                sendSMS(requireActivity(), number, "Hello, this is a test message!")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val start_mon_btn = requireView().findViewById<MaterialButton>(R.id.start_mon_btn)

        var isActive = requireContext().isServiceRunning(LocationMonitor::class.java)
        if(!isActive) {
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            start_mon_btn.backgroundTintList = colorStateList
        }
        else {
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
            start_mon_btn.backgroundTintList = colorStateList
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}