package club.ourail.cumtdtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    class Stop(
        var code: String,
        var stopid: String,
        var stopname: String,
        var stopdistance: Double
    )


    private var lat = -9999.99 //by Delegates.notNull<Double>()
    private var lon = -9999.99 //by Delegates.notNull<Double>()
    private val url = "https://developer.cumtd.com/"
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"

    private var stops = mutableListOf<Stop>()
    private lateinit var listview: ListView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: StopListAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var appBarLayout: com.google.android.material.appbar.MaterialToolbar

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

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

        listview = findViewById(R.id.list)
        adapter = StopListAdapter(stops, applicationContext)
        listview.adapter = adapter


        swipeLayout = findViewById(R.id.main)
        swipeLayout.isEnabled = true
        swipeLayout.setOnRefreshListener()
        {
            getLocation()
        }

        listview.setOnItemClickListener()
        { parent, view, position, id ->
            val element = stops[position].stopid // The item that was clicked
            val title = stops[position].stopname // The item that was clicked
            val intent = Intent(this, StopActivity::class.java)
            intent.putExtra("stopid", element)
            intent.putExtra("title", title)
            startActivity(intent)
        }


        listview.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {
                swipeLayout.isEnabled = false
            }

            override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
                if (p1 == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    swipeLayout.isEnabled = true
                }
            }
        })
    }


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

        val retrofit =
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val service = retrofit.create(getstopsbylatlon::class.java)
        val call = service.getStopByLatLon(lat.toString(), lon.toString(), key)
        call.enqueue(object : Callback<StopResponse> {
            override fun onResponse(
                call: Call<StopResponse>,
                response: Response<StopResponse>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    input.clear()
                    for (i in 0 until apiResponse.stops.size) {
                        val code = apiResponse.stops[i].StopName!!
                        val distance = (apiResponse.stops[i].StopDistance!!).toDouble()
                        val name = apiResponse.stops[i].StopName!!
                        val id = apiResponse.stops[i].StopId!!

                        val stop = Stop(code, id, name, distance)
                        input.add(stop)
                    }

                    adapter.notifyDataSetChanged()
                    swipeLayout.isRefreshing = false
                    Snackbar.make(swipeLayout, "Stations Updated", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StopResponse>, t: Throwable) {
                swipeLayout.isRefreshing = false
                Snackbar.make(swipeLayout, t.message.toString(), Snackbar.LENGTH_SHORT).show()
            }
        })
    }


    private fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 100f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {}

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
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
        getNearbyStopList(stops)
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
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getLocation()
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Snackbar.make(
                            swipeLayout,
                            "Permission denied. Please enable location manually.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
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
        var itemId = item?.itemId
        if (itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
//            val element = stops[0].stopid // The item that was clicked
//            val title = stops[0].stopname // The item that was clicked
//            intent.putExtra("stopid", element)
//            intent.putExtra("title", title)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}