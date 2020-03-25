package club.ourail.cumtdtracker

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class StopActivity : AppCompatActivity() {
    class Bus(
        var color: String,
        var headSign: String,
        var dest: String,
        var expectedMins: Int,
        var isIstop: Boolean
    )

    private val url = "https://developer.cumtd.com/"
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"
    private lateinit var stopId: String

    private var buses = mutableListOf<Bus>()
    private lateinit var listview: ListView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: BusListAdapter
    private lateinit var warning: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        stopId = intent.extras?.getString("stopid")!!
        val title = intent.extras?.getString("title")!!

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        val currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } // Night mode is not active, we're using the light theme
        }

        toolbar = findViewById(R.id.toolbar2)
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        swipeLayout = findViewById(R.id.stop)
        swipeLayout.setOnRefreshListener {
            getDepartures(buses)
        }


        listview = findViewById(R.id.list2)
        adapter = BusListAdapter(buses, applicationContext)
        listview.adapter = adapter
        warning = findViewById(R.id.warning)


        getDepartures(buses)
    }


    private fun getDepartures(input: MutableList<StopActivity.Bus>) {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
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
                        for (i in 0 until apiResponse.Buses.size) {
                            val color = apiResponse.Buses[i].route!!.Color!!
                            val expected = apiResponse.Buses[i].ExpectedMins!!
                            val headsign = apiResponse.Buses[i].HeadSign!!
                            val destination = apiResponse.Buses[i].trip!!.TripHeadsign!!
                            val isIstop = apiResponse.Buses[i].IsIstop!!
                            val bus =
                                StopActivity.Bus(color, headsign, destination, expected, isIstop)
                            input.add(bus)

                        }
                    }

                    adapter.notifyDataSetChanged()
                    swipeLayout.isRefreshing = false
                    Snackbar.make(
                        swipeLayout,
                        "Departures Updated",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<BusResponse>, t: Throwable) {
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