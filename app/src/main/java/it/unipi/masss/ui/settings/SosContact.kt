import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.myapplication.R

class SosContact : ConstraintLayout {
    private var textView: TextView? = null
    private var cancelButton: FloatingActionButton? = null
    private var contactId: Long = 0

    constructor(context: Context, text: String, id: Long) : super(context) {
        this.contactId = id
        init(text)
    }

    constructor(context: Context, attrs: AttributeSet, text: String, id: Long) : super(context, attrs) {
        this.contactId = id
        init(text)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, text: String, id: Long) : super(context, attrs, defStyleAttr) {
        this.contactId = id
        init(text)
    }

    private fun init(text: String) {
        LayoutInflater.from(context).inflate(R.layout.sos_contact, this, true)
        textView = findViewById(R.id.textView4)
        textView?.text = text

        cancelButton = findViewById(R.id.floatingActionButton)
        cancelButton?.setOnClickListener {
            // Remove the SosContact view from its parent layout
            (parent as? ViewGroup)?.removeView(this)

            // Remove the corresponding contact info from SharedPreferences
            val sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("contact_info_$contactId")
            editor.apply()
        }
    }
}
