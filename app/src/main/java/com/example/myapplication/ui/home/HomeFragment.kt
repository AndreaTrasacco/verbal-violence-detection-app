package com.example.myapplication.ui.home

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.LocationMonitor
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.android.material.button.MaterialButton


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val start_mon_btn = view.findViewById<MaterialButton>(R.id.start_mon_btn)

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