package it.unipi.masss.project.ui.home

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import it.unipi.masss.project.BackgroundMonitor
import it.unipi.masss.project.R
import it.unipi.masss.project.databinding.FragmentHomeBinding


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val start_mon_btn = view.findViewById<MaterialButton>(R.id.start_mon_btn)

        start_mon_btn.setOnClickListener {

            // detect if the service is already running
            val sharedPreferences = activity?.getSharedPreferences("BG_SERVICE", Context.MODE_PRIVATE)
            var isActive = sharedPreferences?.getBoolean("isServiceRunning", false) ?: false

            // send a notification to the user
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannelId = "protectron"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(notificationChannelId, "Notification", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val notificationBuilder = NotificationCompat.Builder(requireContext(), notificationChannelId)
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setAutoCancel(true)

            if (!isActive) {
                // start background monitoring service
                val intent = Intent(context, BackgroundMonitor::class.java)
                context?.startService(intent)

                notificationBuilder.setContentTitle("PROTECTRON SERVICE ACTIVATED")
                    .setContentText("Background monitoring is now activated")

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
                start_mon_btn.backgroundTintList = colorStateList
            }
            else {
                // stop background monitoring service
                val intent = Intent(context, BackgroundMonitor::class.java)
                context?.stopService(intent)

                notificationBuilder.setContentTitle("PROTECTRON SERVICE DEACTIVATED")
                    .setContentText("Background monitoring is now deactivated")

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
                start_mon_btn.backgroundTintList = colorStateList
            }

            notificationManager.notify(0, notificationBuilder.build())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}