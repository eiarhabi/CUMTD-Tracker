package club.ourail.cumtdtracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class TripListAdapter(val list: MutableList<TripActivity.Trip>) :
    RecyclerView.Adapter<TripHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TripHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size


    override fun onBindViewHolder(holder: TripHolder, position: Int, payloads: MutableList<Any>) {
        val temp = list[position]

        holder.bind(temp)
        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view?.context, StopActivity::class.java)
            intent.putExtra("stopid", temp.stopId)
            intent.putExtra("title", temp.stopName.substringBeforeLast("("))
            view?.context?.startActivity(intent)
        }
    }

    override fun onBindViewHolder(holder: TripHolder, position: Int) {
    }
}