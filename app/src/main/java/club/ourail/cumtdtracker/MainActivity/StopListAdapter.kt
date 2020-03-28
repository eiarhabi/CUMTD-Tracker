package club.ourail.cumtdtracker

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import club.ourail.cumtdtracker.MainActivity.MainActivity


class StopListAdapter(items: MutableList<MainActivity.Stop>, context: Context) :
    ArrayAdapter<MainActivity.Stop>(context, R.layout.listview_text, items) {
    private class StopListHolder {
        internal var name: TextView? = null
        internal var distance: TextView? = null
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


        var i = attraction.stopdistance
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getString("isMetric", "imperial")!! == "metric"
        ) {
            i *= 0.3048
            var str = if (i > 1200) (i / 1000).round(1).toString() + " km" else i.toInt()
                .toString() + " m"
            Holder.distance!!.text = str
        } else {
            var str = if (i > 4000) (i / 5280).round(1).toString() + " mi." else i.toInt()
                .toString() + " ft."
            Holder.distance!!.text = str
        }

        return view
    }
}

