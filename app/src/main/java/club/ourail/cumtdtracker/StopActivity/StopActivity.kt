package club.ourail.cumtdtracker

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class StopActivity : AppCompatActivity() {
    class Bus(
        var color: String,
        var headSign: String,
        var dest: String,
        var expectedMins: Int,
        var isIstop: Boolean,
        var tripId: String,
        var isMonitored: Boolean,
        var stopId: String,
        var colorText: String,
        var shortName: String,
        var longName: String,
        var direction: String
    )

    private val url = "https://developer.cumtd.com/"
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"
    private lateinit var stopId: String

    private var buses = mutableListOf<Bus>()
    private lateinit var listview: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: RecyclerView.Adapter<BusHolder>
    private lateinit var warning: TextView
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var fab: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        stopId = intent.extras?.getString("stopid")!!
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
        toolbar = findViewById(R.id.toolbar2)
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(arrow)

        fab = findViewById(R.id.fab2)
        fab.setOnClickListener {
            swipeLayout.isRefreshing = true
            getDepartures(buses)
        }

        swipeLayout = findViewById(R.id.stop)
        swipeLayout.setOnRefreshListener {
            getDepartures(buses)
        }

        manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = BusListAdapter(buses)
        listview = findViewById(R.id.list2)
        listview.layoutManager = manager
        listview.adapter = adapter
        listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) fab.hide() else fab.show()
            }
        })

        warning = findViewById(R.id.warning)

        swipeLayout.isRefreshing = true
        getDepartures(buses)
    }


    private fun getDepartures(input: MutableList<Bus>) {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit =
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        val service = retrofit.create(getdeparturesbystop::class.java)
        val call = service.getDeparturesByStop(stopId, 60, key)
        call.enqueue(object : Callback<BusResponse> {
            override fun onResponse(
                call: Call<BusResponse>,
                response: Response<BusResponse>
            ) {
                swipeLayout.isRefreshing = false
                if (response.isSuccessful) {
                    Snackbar.make(
                        swipeLayout, "Departures Updated", Snackbar.LENGTH_SHORT
                    ).setAnchorView(R.id.fab2).show()
                    val apiResponse = response.body()!!
                    input.clear()

                    if (apiResponse.departures.isEmpty()) {
                        warning.text = getString(R.string.no_departure)
                    } else {
                        warning.text = ""
                        for (i in apiResponse.departures) {
                            val color = i.route.route_color
                            val expected = i.expected_mins
                            val headsign = i.headsign
                            val destination = i.trip.trip_headsign
                            val isIstop = i.is_istop
                            val tripId = i.trip.trip_id
                            val isMornitored = i.is_monitored
                            val colortext = i.route.route_text_color
                            val shortname = i.route.route_short_name
                            val longname = i.route.route_long_name
                            val direction = i.trip.direction

                            val bus = Bus(
                                color,
                                headsign,
                                destination,
                                expected,
                                isIstop,
                                tripId,
                                isMornitored,
                                stopId,
                                colortext,
                                shortname,
                                longname,
                                direction
                            )
                            input.add(bus)
                        }
                    }
                    listview.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                } else {
                    listview.visibility = View.INVISIBLE
                    val i = GsonBuilder().create()
                        .fromJson(response.errorBody()?.string(), StopResponse::class.java).status
                    warning.text = "Error code ${i.code}:\n${i.msg}."
                }

            }

            override fun onFailure(call: Call<BusResponse>, t: Throwable) {
                swipeLayout.isRefreshing = false
                listview.visibility = View.INVISIBLE
                warning.text = t.message
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.stop_activity_menu, menu)
        menu.findItem(R.id.website).setOnMenuItemClickListener {
            val url = "https://mtd.org/maps-and-schedules/bus-stops/info/$stopId"
            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(url))

            return@setOnMenuItemClickListener true
        }
        return super.onCreateOptionsMenu(menu)
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