package it.unipi.masss.ui.home

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
import it.unipi.masss.R
import it.unipi.masss.databinding.FragmentHomeBinding
import com.google.android.material.button.MaterialButton
import it.unipi.masss.recordingservice.RecordingService
import it.unipi.masss.Util.isServiceRunning


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startMonBtn = view.findViewById<MaterialButton>(R.id.start_mon_btn)
        if (requireContext().isServiceRunning(RecordingService::class.java)) {
            Log.d("HomeFragment", "Recording service is active")
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
            startMonBtn.backgroundTintList = colorStateList
        } else {
            Log.d("HomeFragment", "Recording service is not active")
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            startMonBtn.backgroundTintList = colorStateList
        }

        startMonBtn.setOnClickListener {

            // detect if the recording service is already running
            if (!requireContext().isServiceRunning(RecordingService::class.java)) {
                // Start Recording service
                Intent(context?.applicationContext, RecordingService::class.java).also {
                    it.action = RecordingService.Action.START.toString()
                    context?.applicationContext?.startService(it)
                }

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
                startMonBtn.backgroundTintList = colorStateList
            } else {
                Intent(context?.applicationContext, RecordingService::class.java).also {
                    it.action = RecordingService.Action.STOP.toString()
                    context?.applicationContext?.startService(it)
                }

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
                startMonBtn.backgroundTintList = colorStateList
            }

        }
    }

    override fun onResume() {
        super.onResume()
        val startMonBtn = requireView().findViewById<MaterialButton>(R.id.start_mon_btn)

        if (!requireContext().isServiceRunning(RecordingService::class.java)) {
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            startMonBtn.backgroundTintList = colorStateList
        } else {
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
            startMonBtn.backgroundTintList = colorStateList
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}