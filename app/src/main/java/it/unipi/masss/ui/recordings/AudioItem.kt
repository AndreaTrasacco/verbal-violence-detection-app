package it.unipi.masss.ui.recordings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import it.unipi.masss.R
import java.io.File

class AudioItem : ConstraintLayout {
    private var textView: TextView? = null
    private var contactId: String = "audio"

    constructor(context: Context, text: String, id: String) : super(context) {
        this.contactId = id
        init(text)
    }

    constructor(context: Context, attrs: AttributeSet, text: String, id: String) : super(context, attrs) {
        this.contactId = id
        init(text)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, text: String, id: String) : super(context, attrs, defStyleAttr) {
        this.contactId = id
        init(text)
    }

    private fun init(text: String) {
        LayoutInflater.from(context).inflate(R.layout.audio_item, this, true)
        textView = findViewById(R.id.audioText)
        textView?.text = text

        this.id = id.hashCode()
    }


    fun onDeleteButtonClick(view: View, audioPath: String, audioListLayout: LinearLayout, scrollViewLayout: ScrollView) {
        // Remove the AudioItem from the layout
        (parent as? ViewGroup)?.removeView(this)

        // Delete the associated file
        val fileToDelete = File(audioPath + contactId)
        if (fileToDelete.exists()) {
            fileToDelete.delete()
        }

        // Request layout for audioListLayout and scrollViewLayout
        audioListLayout.requestLayout()
        scrollViewLayout.requestLayout()
    }
}

