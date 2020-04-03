package club.ourail.cumtdtracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import club.ourail.cumtdtracker.MainActivity.MainActivity

class StopListAdapter(val list: MutableList<MainActivity.Stop>) :
    RecyclerView.Adapter<StopHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopHolder {
        val inflater = LayoutInflater.from(parent.context)
        return StopHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: StopHolder, position: Int) {
        val temp = list[position]

        holder.bind(temp)
        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view?.context, StopActivity::class.java)
            intent.putExtra("title", temp.stopname)
            intent.putExtra("stopid", temp.stopid)
            view?.context?.startActivity(intent)
        }
    }
}


