package club.ourail.cumtdtracker

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView

class BusHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.listview_bus, parent, false)) {

    val destination = itemView.findViewById(R.id.bus_destination) as TextView
    val route = itemView.findViewById(R.id.bus_sign) as TextView
    val route_number = itemView.findViewById(R.id.route_number) as TextView
    val mins = itemView.findViewById(R.id.mins) as TextView
    val iStop = itemView.findViewById(R.id.iStop) as ImageView
    val circle = itemView.findViewById(R.id.circle) as ImageView
    val monitored = itemView.findViewById(R.id.is_monitored) as ImageView

    fun bind(bus: StopActivity.Bus) {
        destination.text = bus.dest
        if (bus.direction == "N/A")
            route.text = bus.longName
        else
            route.text = "${bus.longName.substringBefore(' ')} - ${bus.direction}"
        mins.text = bus.expectedMins.toString()
        iStop.visibility = if (bus.isIstop) View.VISIBLE else View.INVISIBLE
        monitored.visibility = if (bus.isMonitored) View.VISIBLE else View.INVISIBLE
        route_number.text = bus.shortName
        route_number.setTextColor(Color.parseColor("#${bus.colorText}"))

        val c = ContextCompat.getDrawable(itemView.context, R.drawable.ic_circle)
        DrawableCompat.setTint(
            DrawableCompat.wrap(c!!),
            Color.parseColor("#" + bus.color)
        )
        circle.setImageDrawable(c)

        if (bus.isMonitored) {
            monitored.setBackgroundResource(R.drawable.ic_rss_feed_white_24dp_animated)
            val frameAnimation = monitored.background as AnimationDrawable
            frameAnimation.start()
        }
    }
}