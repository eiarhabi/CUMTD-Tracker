package club.ourail.cumtdtracker.MainActivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import club.ourail.cumtdtracker.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    class Stop(
        var code: String,
        var stopid: String,
        var stopname: String,
        var stopdistance: Double
    )


    private var lat = -9999.99
    private var lon = -9999.99
    private val url = "https://developer.cumtd.com/"//
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"//

    private var stops = mutableListOf<Stop>()
    private lateinit var warning: TextView
    private lateinit var listview: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: RecyclerView.Adapter<StopHolder>
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var appBarLayout: com.google.android.material.appbar.MaterialToolbar

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        appBarLayout = findViewById(R.id.appbar_layout)
        setSupportActionBar(appBarLayout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()

        manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = StopListAdapter(stops)
        listview = findViewById(R.id.list)
        listview.layoutManager = manager
        listview.adapter = adapter

        warning = findViewById(R.id.warning_main)

        swipeLayout = findViewById(R.id.main)
        swipeLayout.setOnRefreshListener {
            getLocation()
        }

        swipeLayout.isRefreshing = true
        checkPermission()

        getLocation()
    }

    @Throws(Exception::class)
    private fun getLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    getNearbyStopList(stops)
                }
            }
    }

    private fun getNearbyStopList(input: MutableList<Stop>) {
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
        val call = service.getStopByLatLon(lat.toString(), lon.toString(), key)
        call.enqueue(object : Callback<StopResponse> {
            override fun onResponse(
                call: Call<StopResponse>,
                response: Response<StopResponse>
            ) {
                if (response.isSuccessful) {
                    input.clear()
                    for (i in response.body()!!.stops) {
                        val code = i.StopCode!!
                        val distance = i.StopDistance!!.toDouble()
                        val name = i.StopPoints[0].StopName!!.substringBeforeLast("(")
                            .replace(" & ", " and ")
                        val id = i.StopId!!

                        val stop = Stop(code, id, name, distance)
                        input.add(stop)
                    }

                    adapter.notifyDataSetChanged()
                    swipeLayout.isRefreshing = false
                    Snackbar.make(swipeLayout, "Nearby Stations Updated", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<StopResponse>, t: Throwable) {
                swipeLayout.isRefreshing = false
                warning.text = t.message
            }
        })
    }


    fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.smallestDisplacement = 50f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationCallback = object : LocationCallback() {}
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        ).addOnSuccessListener { getLocation() }
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        adapter.notifyDataSetChanged()
    }

    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        warning.text = "Permission denied. Please enable location manually."
                    }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.search -> {
                startActivity(Intent(this, SearchActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
