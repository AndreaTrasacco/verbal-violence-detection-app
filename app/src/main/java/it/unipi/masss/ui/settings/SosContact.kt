import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.unipi.masss.R

class SosContact : ConstraintLayout {
    private var textView: TextView? = null
    private var cancelButton: FloatingActionButton? = null
    private var contactId: Long = 0

    constructor(context: Context, name: String, number: String, id: Long) : super(context) {
        this.contactId = id
        init(name, number)
    }

    constructor(context: Context, attrs: AttributeSet, name: String, number: String, id: Long) : super(context, attrs) {
        this.contactId = id
        init(name, number)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, name: String, number: String, id: Long) : super(context, attrs, defStyleAttr) {
        this.contactId = id
        init(name, number)
    }

    private fun init(name: String, number: String) {
        LayoutInflater.from(context).inflate(R.layout.sos_contact, this, true)
        textView = findViewById(R.id.contactText)
        textView?.text = name

        textView = findViewById(R.id.numberText)
        textView?.text = number

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
