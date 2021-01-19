package club.ourail.cumtdtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import club.ourail.cumtdtracker.MainActivity.MainActivity

class StopHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.listview_text, parent, false)) {

    val name = itemView.findViewById<View>(R.id.stop_name) as TextView
    val distance = itemView.findViewById(R.id.stop_distance) as TextView

    fun bind(stop: MainActivity.Stop) {
        name.text = stop.stopname

        var i = stop.stopdistance
        if (i == 1145141919810.0) {
            return
        }

        if (PreferenceManager.getDefaultSharedPreferences(itemView.context)
                .getString("isMetric", "imperial")!! == "metric"
        ) {
            i *= 0.3048
            val str = if (i > 800) (i / 1000).round(1).toString() + " km" else i.toInt()
                .toString() + " m"
            distance.text = str
        } else {
            val str = if (i > 800) (i / 5280).round(1).toString() + " mi." else i.toInt()
                .toString() + " ft."
            distance.text = str
        }
    }
}