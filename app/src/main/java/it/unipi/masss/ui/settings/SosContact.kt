import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.unipi.masss.ProtectronApplication
import it.unipi.masss.ProtectronApplication.Companion.SHARED_PREF
import it.unipi.masss.R
import it.unipi.masss.ui.recordings.AudioItem
import it.unipi.masss.ui.recordings.RecordingsFragment
import java.io.File

class SosContact : ConstraintLayout {
    private var textView: TextView? = null
    private var cancelButton: FloatingActionButton? = null
    private var contactId: Long = 0
    private var name: String = ""
    private var number: String = ""

    constructor(context: Context, name: String, number: String, id: Long) : super(context) {
        init(name, number, id)
    }

    constructor(context: Context, attrs: AttributeSet, name: String, number: String, id: Long) : super(context, attrs) {
        init(name, number, id)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, name: String, number: String, id: Long) : super(context, attrs, defStyleAttr) {
        init(name, number, id)
    }

    private fun init(name: String, number: String, id: Long) {
        this.name = name
        this.number = number
        this.contactId = id
    }

    private fun onCancelButtonClick(view: View, adapter: RecyclerView.Adapter<*>, position: Int, contactList: MutableList<SosContact>) {
        // Remove the SosContact view from its parent layout
        (parent as? ViewGroup)?.removeView(this)

        // Remove the corresponding contact info from SharedPreferences
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("contact_info_$contactId")
        editor.apply()

        // Remove the item from the adapter's data set
        contactList.removeAt(position)
        adapter.notifyDataSetChanged()
    }


    fun setData(currentContact: SosContact, adapter: RecyclerView.Adapter<*>, position: Int, contactList: MutableList<SosContact>,){
        this.contactId = currentContact.contactId
        this.name = currentContact.name
        this.number = currentContact.number
        this.contactId = currentContact.contactId

        textView = findViewById(R.id.contactText)
        textView?.text = name
        textView = findViewById(R.id.numberText)
        textView?.text = number


        this.id = currentContact.contactId.hashCode()

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // width
            LinearLayout.LayoutParams.WRAP_CONTENT  // height
        )
        params.topMargin = 25  // set top margin here
        this.layoutParams = params

        this.findViewById<FloatingActionButton>(R.id.cancelButton).setOnClickListener { view ->
            this.onCancelButtonClick(view, adapter, position, contactList)
        }
    }
}
