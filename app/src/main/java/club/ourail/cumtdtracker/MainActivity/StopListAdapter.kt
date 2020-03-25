package club.ourail.cumtdtracker

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class StopListAdapter(items: MutableList<MainActivity.Stop>, context: Context) :
    ArrayAdapter<MainActivity.Stop>(context, R.layout.listview_text, items) {
    private class StopListHolder {
        // var stop = Stop(code, id, lat, lon, name, distance)
        internal var name: TextView? = null
        internal var distance: TextView? = null
//        internal var code: String? = null
//        internal var id: String? = null
//        internal var lat: Double? = null
//        internal var lon: Double? = null
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView


        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.listview_text, parent, false)

        val Holder: StopListHolder = StopListHolder()
        Holder.name = view!!.findViewById<View>(R.id.stop_name) as TextView
        Holder.distance = view.findViewById(R.id.stop_distance) as TextView

        val attraction = getItem(position)
        Holder.name!!.text = attraction!!.stopname

        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getString("isMetric", "0")!! == "1"
        ) {
            Holder.distance!!.text = (attraction.stopdistance * 0.3048).toInt().toString() + " m"
        } else {
            Holder.distance!!.text = attraction.stopdistance.toInt().toString() + " ft"
        }

        return view //super.getView(position, convertView, parent)
    }
}

