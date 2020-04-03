package club.ourail.cumtdtracker

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
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
        var shortName:String,
        var longName: String,
        var direction:String
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

        swipeLayout = findViewById(R.id.stop)
        swipeLayout.setOnRefreshListener {
            getDepartures(buses)
        }

        manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = BusListAdapter(buses)
        listview = findViewById(R.id.list2)
        listview.layoutManager = manager
        listview.adapter = adapter

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
                if (response.isSuccessful) {
                    val apiResponse = response.body()!!
                    input.clear()

                    if (apiResponse.Buses.isEmpty()) {
                        warning.text = getString(R.string.no_departure)
                    } else {
                        warning.text = ""
                        for (i in apiResponse.Buses) {
                            val color = i.route!!.Color!!
                            val expected = i.ExpectedMins!!
                            val headsign = i.HeadSign!!
                            val destination = i.trip!!.TripHeadsign!!
                            val isIstop = i.IsIstop!!
                            val tripId = i.trip!!.TripId!!
                            val isMornitored = i.IsMonitored!!
                            val colortext = i.route!!.ColorText!!
                            val shortname = i.route!!.ShortName!!
                            val longname = i.route!!.LongName!!
                            val direction = i.trip!!.Direction!!

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

                    adapter.notifyDataSetChanged()
                    swipeLayout.isRefreshing = false
                    Snackbar.make(
                        swipeLayout, "Departures Updated", Snackbar.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<BusResponse>, t: Throwable) {
                swipeLayout.isRefreshing = false
//                Snackbar.make(swipeLayout, t.message.toString(), Snackbar.LENGTH_SHORT).show()
                warning.text = t.message
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