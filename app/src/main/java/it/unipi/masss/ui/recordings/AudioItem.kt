package it.unipi.masss.ui.recordings

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.unipi.masss.R
import java.io.File

class AudioItem : ConstraintLayout {
    private var textView: TextView? = null
    private var audioText: String = "text"
    private var audioId: String = "audio"
    private var audioPath: String = "./"

    constructor(context: Context) : super(context)

    constructor(context: Context, text: String, id: String, audioPath: String) : super(context) {
        init(text, id, audioPath)
    }

    constructor(context: Context, attrs: AttributeSet, text: String, id: String, audioPath: String) : super(context, attrs) {
        init(text, id, audioPath)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, text: String, id: String, audioPath: String) : super(context, attrs, defStyleAttr) {
        init(text, id, audioPath)
    }

    private fun init(text: String, id: String, audioPath: String) {
        this.audioText = text
        this.audioId = id
        this.audioPath = audioPath
    }


    fun onDeleteButtonClick(view: View, audioPath: String, adapter: RecyclerView.Adapter<*>, position: Int, audioItemList: MutableList<AudioItem>) {
        // Remove the AudioItem from the layout
        (parent as? ViewGroup)?.removeView(this)

        // Delete the associated file
        val fileToDelete = File(audioPath + audioId)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }

        // Remove the item from the adapter's data set
        audioItemList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    fun onPlayButtonClick(view: View, fragment: RecordingsFragment) {
        // If the audio is playing, stop it
        if (fragment.mediaPlayer?.isPlaying == true) {
            fragment.mediaPlayer?.stop()
            fragment.mediaPlayer?.release()
            fragment.mediaPlayer = null

            val playingAudioItem = fragment.view?.findViewById<ConstraintLayout>(fragment.currentlyPlaying.hashCode())
            val playingButton = playingAudioItem?.findViewById<FloatingActionButton>(R.id.playButton)
            playingButton?.setImageResource(android.R.drawable.ic_media_play)
        }
        if(fragment.currentlyPlaying != this.audioId){
            // Otherwise, start playing the audio
            fragment.mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath + audioId)
                prepare()
                start()

                setOnCompletionListener {
                    val endedAudioItem = fragment.view?.findViewById<ConstraintLayout>(audioId.hashCode())
                    val endedButton = endedAudioItem?.findViewById<FloatingActionButton>(R.id.playButton)
                    endedButton?.setImageResource(android.R.drawable.ic_media_play)
                    fragment.currentlyPlaying = null
                }
            }
            fragment.currentlyPlaying = this.audioId
            val currentAudioItem = fragment.view?.findViewById<ConstraintLayout>(audioId.hashCode())
            val playButton = currentAudioItem?.findViewById<FloatingActionButton>(R.id.playButton)
            playButton?.setImageResource(android.R.drawable.ic_media_pause)
        }
        else{
            fragment.currentlyPlaying = null
        }
    }

    fun setData(currentAudio: AudioItem, fragment: RecordingsFragment, adapter: RecyclerView.Adapter<*>, position: Int, audioItemList: MutableList<AudioItem>,){
        this.audioText = currentAudio.audioText
        this.audioId = currentAudio.audioId
        this.audioPath = currentAudio.audioPath

        textView = findViewById(R.id.audioText)
        textView?.text = currentAudio.audioText

        this.id = currentAudio.audioId.hashCode()

        if(fragment.currentlyPlaying == audioId){
            val currentAudioItem = fragment.view?.findViewById<ConstraintLayout>(audioId.hashCode())
            val playButton = currentAudioItem?.findViewById<FloatingActionButton>(R.id.playButton)
            playButton?.setImageResource(android.R.drawable.ic_media_pause)
        }

        this.findViewById<FloatingActionButton>(R.id.deleteButton).setOnClickListener { view ->
            this.onDeleteButtonClick(view, audioPath, adapter, position, audioItemList)
        }

        this.findViewById<FloatingActionButton>(R.id.playButton).setOnClickListener { view ->
            this.onPlayButtonClick(view, fragment)
        }
    }
}

