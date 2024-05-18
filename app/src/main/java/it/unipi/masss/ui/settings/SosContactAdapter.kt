import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.unipi.masss.R
import it.unipi.masss.ui.recordings.AudioItem
import it.unipi.masss.ui.recordings.RecordingsFragment
import it.unipi.masss.ui.settings.SettingsFragment

class SosContactAdapter(
    private var contactList: MutableList<SosContact>
) : RecyclerView.Adapter<SosContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(val row: SosContact) : RecyclerView.ViewHolder(row)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.sos_contact,
            parent, false)

        val layoutParams = view.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        view.layoutParams = layoutParams

        val sosContact = SosContact(context = parent.context, name = "", number = "", id = 0)
        sosContact.addView(view)
        return ContactViewHolder(sosContact)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.row.setData(contact, this, position, contactList)
    }

    override fun getItemCount() = contactList.size
}
