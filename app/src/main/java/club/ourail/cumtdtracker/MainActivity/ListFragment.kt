package club.ourail.cumtdtracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import club.ourail.cumtdtracker.MainActivity.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_second.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ListFragment : Fragment() {
    private var list = mutableListOf<MainActivity.Stop>()
    private val url = "https://developer.cumtd.com/"//
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"//

    private lateinit var fab: FloatingActionButton
    private lateinit var ac: MainActivity
    private lateinit var listview: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: RecyclerView.Adapter<StopHolder>
    private lateinit var manager: RecyclerView.LayoutManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isStart = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        ac = activity as MainActivity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        swipeLayout = view.findViewById(R.id.srl_fm) as SwipeRefreshLayout
        swipeLayout.setOnRefreshListener {
            getLocation()
        }

        fab = view.findViewById(R.id.fab_fragment_list)

        listview = view.findViewById(R.id.list_fm) as RecyclerView
        adapter = StopListAdapter(list)

        manager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adapter = StopListAdapter(list)
        listview.layoutManager = manager
        listview.adapter = adapter
        listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    fab.hide()
                } else {
                    fab.show()
                }
            }
        })

        fab.setOnClickListener { getLocation() }

        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun getLocation() {
        FusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener { location ->
            if (location != null)
                getNearbyStopList(list, location)
        }
    }

    private fun getNearbyStopList(input: MutableList<MainActivity.Stop>, location: Location) {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
        val retrofit =
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

        val service = retrofit.create(getstopsbylatlon::class.java)
        val call = service.getStopByLatLon(
            location.latitude.toString(),
            location.longitude.toString(),
            key
        )
        call.enqueue(object : Callback<StopResponse> {
            override fun onResponse(
                call: Call<StopResponse>,
                response: Response<StopResponse>
            ) {
                list.clear()
                swipeLayout.isRefreshing = false
                if (response.isSuccessful) {
                    for (i in response.body()!!.stops) {
                        val code = i.code
                        val distance = i.distance.toDouble()
                        val name = i.stop_name
                        val id = i.stop_id
                        val stop = MainActivity.Stop(code, id, name, distance, 0)
                        input.add(stop)
                    }

                    Snackbar.make(
                        activity!!.findViewById(R.id.c_layout),
                        "Nearby Stations Updated",
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(R.id.fab_fragment_list).show()
                    listview.adapter?.notifyDataSetChanged()
                    swipeLayout.isRefreshing = false
                } else {
                    val i = GsonBuilder().create()
                        .fromJson(response.errorBody()?.string(), StopResponse::class.java).status
                    warning.text = "Error code ${i.code}:\n${i.msg}."
                }
            }

            override fun onFailure(call: Call<StopResponse>, t: Throwable) {
                swipeLayout.isRefreshing = false
            }
        })
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.smallestDisplacement = 50f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationCallback = LocationCallback()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* loop */
            ).addOnSuccessListener {
                if (isStart) {
                    isStart = false
                    getLocation()
                }
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
