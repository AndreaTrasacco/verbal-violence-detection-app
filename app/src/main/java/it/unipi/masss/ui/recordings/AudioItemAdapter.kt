import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.unipi.masss.R
import it.unipi.masss.ui.recordings.AudioItem
import it.unipi.masss.ui.recordings.RecordingsFragment

class AudioItemAdapter(
    private var audioItemList: MutableList<AudioItem>,
    private val fragment: RecordingsFragment
) : RecyclerView.Adapter<AudioItemAdapter.AudioItemViewHolder>() {

    class AudioItemViewHolder(val row: AudioItem) : RecyclerView.ViewHolder(row)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.audio_item,
            parent, false)

        val layoutParams = view.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        view.layoutParams = layoutParams

        val audioItem = AudioItem(context = parent.context, text = "", id = "", audioPath = "")
        audioItem.addView(view)
        return AudioItemViewHolder(audioItem)
    }

    override fun onBindViewHolder(holder: AudioItemViewHolder, position: Int) {
        val audioItem = audioItemList[position]
        holder.row.setData(audioItem, fragment, this, position, audioItemList)
    }

    override fun getItemCount() = audioItemList.size
}
