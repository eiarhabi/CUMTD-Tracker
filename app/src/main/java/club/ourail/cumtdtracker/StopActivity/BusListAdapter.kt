package club.ourail.cumtdtracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class BusListAdapter(val list: MutableList<StopActivity.Bus>) :
    RecyclerView.Adapter<BusHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BusHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BusHolder, position: Int) {
        val temp = list[position]

        holder.bind(temp)
        holder.itemView.setOnClickListener { view ->
            val temp = list[position]
            val intent = Intent(view?.context, TripActivity::class.java)
            intent.putExtra("tripid", temp.tripId)
            intent.putExtra("title", "${temp.shortName} ${temp.longName} ${temp.direction}")
            intent.putExtra("stopid", temp.stopId)
            view?.context?.startActivity(intent)
        }
    }
}