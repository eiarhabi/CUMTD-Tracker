package club.ourail.cumtdtracker

import android.content.Context
import android.os.AsyncTask
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class ParseJson(var jsonData: String, var context: Context, var listview: ListView) :
    AsyncTask<Void, Void, String>() {

    private var stops = mutableListOf<Stop>()


    class Stop(
        var code: String,
        var stopid: String,
        var stoplat: Float,
        var stoplon: Float,
        var stopname: String
    ) {
    }

    private fun parse(): String? {
        val json = JSONArray(jsonData)
        var jsonObject: JSONObject

        stops.clear()
        var stop: Stop

        for (i in 0 until json.length()) {
                jsonObject = json.getJSONObject(i)
                for (j in 0 until jsonObject.getJSONArray("stop_points").length()) {
                    val code = jsonObject.getString("code")
                    val name = jsonObject.getString("stop_name")
                    var temp = jsonObject.getJSONArray("stop_points").getJSONObject(i)
                    val id = temp.getString("id")
                    val lat = temp.getString("stop_lat").toFloat()
                    val lon = temp.getString("stop_lon").toFloat()

                    stop = Stop(code, id, lat, lon, name)
                    stops.add(stop)
                }
        }
        return "1"
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Toast.makeText(context, "Parsing JSON", Toast.LENGTH_SHORT).show()
    }

    override fun doInBackground(vararg p0: Void?): String? {
        return parse()
    }

    override fun onPostExecute(jsonData: String) {
        listview.adapter = ArrayAdapter(context, listview.id, stops)
    }
}