package club.ourail.cumtdtracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TripHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.listview_trip, parent, false)) {

    private var name = itemView.findViewById(R.id.stop_name_trip) as TextView
    private var time = itemView.findViewById(R.id.stop_time) as TextView
    private var layout = itemView.findViewById(R.id.listview_trip_layout) as LinearLayout

    fun bind(trip: TripActivity.Trip) {
        name.text = trip.stopName.replace(" & ", " and ")
        var str = trip.departureTime.substringBeforeLast(":")
        if (str.substringBefore(":").toInt() >= 24) {
            val temp = str.substringBefore(":").toInt() - 24
            str = "0" + temp.toString() + ":" + str.substringAfter(":")
        }
        time.text = str
    }
}