package club.ourail.cumtdtracker

import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateFormat.getTimeFormat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat


class TripActivity : AppCompatActivity() {
    class Trip(
        var departureTime: String,
        var stopName: String,
        var stopId: String
    )

    private val url = "https://developer.cumtd.com/"
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"
    private lateinit var stopId: String
    private lateinit var tripId: String
    private lateinit var format: DateFormat

    private var list = mutableListOf<Trip>()
    private lateinit var listview: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: RecyclerView.Adapter<TripHolder>
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var warning: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var smoothScroller: SmoothScroller
    private lateinit var tab: BottomNavigationView
    private lateinit var fragmentList: Fragment
    private lateinit var fragmentMap: Fragment
    private lateinit var fragmentManager: FragmentManager
    private lateinit var viewPager: ViewPager
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        tripId = intent.extras?.getString("tripid")!!
        stopId = intent.extras?.getString("stopid")!!
        Log.e("id", stopId)
        val title = intent.extras?.getString("title")!!

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } // Night mode is not active, we're using the light theme
        }
        val arrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        toolbar = findViewById(R.id.toolbar_trip)
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(arrow)

        fragmentManager = supportFragmentManager
        fragmentList = Fragment()
        fragmentMap = Fragment()

        fab = findViewById(R.id.fab_trip)

        manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = TripListAdapter(list)
        listview = findViewById(R.id.list_trip)
        listview.layoutManager = manager
        listview.adapter = adapter
        listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) fab.hide() else fab.show()
            }
        })

        smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        format = getTimeFormat(this)
        getDepartures(list)
    }


    private fun getDepartures(input: MutableList<Trip>) {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .build()
        val retrofit =
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        val service = retrofit.create(getstoptimesbytrip::class.java)
        val call = service.getStopTimeByTrip(tripId, key)
        call.enqueue(object : Callback<TripResponse> {
            override fun onResponse(
                call: Call<TripResponse>,
                response: Response<TripResponse>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    var j = 0
                    if (apiResponse.stop_times.isNotEmpty() && apiResponse.status.code == 200) {
                        input.clear()
                        for (i in apiResponse.stop_times) {
                            val name = i.stop_point.stop_name
                            val time = i.departure_time
                            val id = i.stop_point.stop_id.substringBeforeLast(":")
                            if (id == stopId)
                                j = input.size

                            val bus = Trip(time, name, id)
                            input.add(bus)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    listview.post {
                        smoothScroller.targetPosition = j
                        manager.startSmoothScroll(smoothScroller)
                    }
                } else {
                    val i = GsonBuilder().create()
                        .fromJson(response.errorBody()?.string(), StopResponse::class.java).status
                    warning.text = "Error code ${i.code}:\n${i.msg}."
                }
            }

            override fun onFailure(call: Call<TripResponse>, t: Throwable) {
                swipeLayout.isRefreshing = false
                Snackbar.make(swipeLayout, t.message.toString(), Snackbar.LENGTH_SHORT).show()
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}