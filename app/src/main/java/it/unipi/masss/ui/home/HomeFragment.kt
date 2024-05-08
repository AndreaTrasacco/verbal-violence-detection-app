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
import it.unipi.masss.LocationMonitor
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
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val start_mon_btn = view.findViewById<MaterialButton>(R.id.start_mon_btn)
        if (requireContext().isServiceRunning(RecordingService::class.java)) {
            Log.d("HomeFragment", "Recording service is active")
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
            start_mon_btn.backgroundTintList = colorStateList
        } else {
            Log.d("HomeFragment", "Recording service is not active")
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            start_mon_btn.backgroundTintList = colorStateList
        }

        start_mon_btn.setOnClickListener {

            // detect if the services are already running
            var isActiveRecording = requireContext().isServiceRunning(RecordingService::class.java)

            if (!isActiveRecording) {
                // Start Recording service
                Intent(context?.applicationContext, RecordingService::class.java).also {
                    it.action = RecordingService.Action.START.toString()
                    context?.applicationContext?.startService(it)
                }

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
                start_mon_btn.backgroundTintList = colorStateList
            } else {
                Intent(context?.applicationContext, RecordingService::class.java).also {
                    it.action = RecordingService.Action.STOP.toString()
                    context?.applicationContext?.startService(it)
                }

                // set color of button
                val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
                start_mon_btn.backgroundTintList = colorStateList
            }

        }
    }

    override fun onResume() {
        super.onResume()
        val start_mon_btn = requireView().findViewById<MaterialButton>(R.id.start_mon_btn)

        //var isActive = requireContext().isServiceRunning(LocationMonitor::class.java) // TODO PER FRANCESCO: Location non decide colore bottone (?)
        var isRecordingActive = requireContext().isServiceRunning(RecordingService::class.java)
        if (!isRecordingActive) {
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            start_mon_btn.backgroundTintList = colorStateList
        } else {
            val colorStateList = ColorStateList.valueOf(Color.parseColor("#470000"))
            start_mon_btn.backgroundTintList = colorStateList
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}