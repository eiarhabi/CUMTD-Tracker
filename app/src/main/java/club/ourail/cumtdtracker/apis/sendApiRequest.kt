//package club.ourail.cumtdtracker
//
//import android.content.Context
//import android.os.AsyncTask
//import android.widget.ListView
//import android.widget.Toast
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.logging.Logger
//
//class SendApiRequest(
//    var url: String,
//    var lat: String,
//    var lon: String,
//    var key: String,
//    var context: Context,
//    var listview: ListView
//) : AsyncTask<Void, Void, String>() {
//
//    private fun getCurrentData(): String {
//        var ret = ""
//        val retrofit =
//            Retrofit.Builder()
//                .baseUrl(url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        val service = retrofit.create(CumtdApi::class.java)
//        val call = service.getStopByLatLon(lat, lon, key)
//        call.enqueue(object : Callback<CumtdApiResponse> {
//            override fun onResponse(
//                call: Call<CumtdApiResponse>,
//                response: Response<CumtdApiResponse>
//            ) {
//                if (response.isSuccessful) {
//                    var apiResponse = response.body()!!
//                    ret = apiResponse.stops!!.toString()
//                    Logger.getLogger(SendApiRequest::class.java.name).severe("aaaaaaaaaaaaaaaaaaaaaaaaa "+ ret.length.toString()+"\n")
//
//                }
//            }
//
//            override fun onFailure(call: Call<CumtdApiResponse>, t: Throwable) {
//                val toast =
//                    Toast.makeText(context, "Call failed", Toast.LENGTH_SHORT)
//                toast.show()
//            }
//        })
//        return ret
//    }
//
//    override fun onPreExecute() {
//        super.onPreExecute()
//        Toast.makeText(context, "Downloading", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun doInBackground(vararg p0: Void?): String {
//        return getCurrentData()
//    }
//
//    override fun onPostExecute(jsonData: String) {
//        super.onPostExecute(jsonData)
//        ParseJson(jsonData, context, listview).execute()
//    }
//
//}