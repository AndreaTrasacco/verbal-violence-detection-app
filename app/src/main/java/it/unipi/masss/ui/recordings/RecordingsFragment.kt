package it.unipi.masss.ui.recordings

import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RecordingsFragment"
    }
}
