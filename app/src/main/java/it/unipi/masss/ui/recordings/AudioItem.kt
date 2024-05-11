
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.unipi.masss.R

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
}
