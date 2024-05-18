package it.unipi.masss.ui.recordings

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.unipi.masss.ProtectronApplication
import it.unipi.masss.R
import it.unipi.masss.databinding.FragmentRecordingsBinding
import java.io.File
import java.util.Date
import java.util.Locale


class RecordingsFragment : Fragment() {

    private var _binding: FragmentRecordingsBinding? = null
    private val binding get() = _binding!!

    var mediaPlayer: MediaPlayer? = null
    var currentlyPlaying: String? = null
    val audioItems = mutableMapOf<String, AudioItem>()
    val audioItemList = mutableListOf<AudioItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRecordingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val audioPath = context?.filesDir?.path + "/"

        val directory = File(audioPath)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        //set padding at the bottom of the setting fragment to account for the navigation bar
        val settingsOuterLinLay = view.findViewById<LinearLayout>(R.id.recordingsOuterLinLay)
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)

        bottomNavigationView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                settingsOuterLinLay.setPadding(0,0,0, bottomNavigationView.height)
                bottomNavigationView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // Get an array of files in the directory
        val files = directory.listFiles()
        Log.d(TAG, "FILES: $files")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Iterate over the files and print their names
        files?.forEach { file ->
            if (file.isFile && file.name.startsWith("recording_")) {
                val date = Date(file.lastModified())
                val dateString = sdf.format(date)

                val audioItem = AudioItem(requireContext(), dateString, file.name, audioPath)
                audioItemList.add(audioItem)
                Log.d("Recordings", "audioitems: $audioItems")
            }
        }

        // Set the adapter for the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = AudioItemAdapter(audioItemList, this)
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onResume() {
        super.onResume()

        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        currentlyPlaying = null
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.adapter?.notifyDataSetChanged()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RecordingsFragment"
    }
}
