package club.ourail.cumtdtracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class BusListAdapter(items: MutableList<StopActivity.Bus>, context: Context) :
    ArrayAdapter<StopActivity.Bus>(context, R.layout.listview_text, items) {
    private class BusListHolder {
        // var stop = Stop(code, id, lat, lon, name, distance)
        internal var destination: TextView? = null
        internal var route: TextView? = null
        internal var mins: TextView? = null
        internal var iStop: ImageView? = null
        internal var circle: ImageView? = null
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //var view = convertView
        val inflater = LayoutInflater.from(context)
        var view = inflater.inflate(R.layout.listview_bus, parent, false)

        var Holder = BusListHolder()
        Holder.destination = view!!.findViewById(R.id.bus_destination) as TextView
        Holder.route = view.findViewById(R.id.bus_sign) as TextView
        Holder.mins = view.findViewById(R.id.mins) as TextView
        Holder.iStop = view.findViewById(R.id.iStop) as ImageView
        Holder.circle = view.findViewById(R.id.circle) as ImageView

        var istop: Drawable? = null
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                istop = ContextCompat.getDrawable(context, R.drawable.ic_istop_black)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                istop = ContextCompat.getDrawable(context, R.drawable.ic_istop_white)
            }
        }

        val bus = getItem(position)
        Holder.destination!!.text = bus!!.dest
        Holder.route!!.text = bus.headSign
        Log.e("color: ", bus.color)
        Holder.mins!!.text = bus.expectedMins.toString()
        if (bus.isIstop) {
            Holder.iStop?.setImageDrawable(istop!!)
        }

        var circle: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_circle)
        DrawableCompat.setTint(
            DrawableCompat.wrap(circle!!),
            Color.parseColor("#" + bus.color)
        )
        Holder.circle!!.setImageDrawable(circle)


        return view //super.getView(position, convertView, parent)
    }
}