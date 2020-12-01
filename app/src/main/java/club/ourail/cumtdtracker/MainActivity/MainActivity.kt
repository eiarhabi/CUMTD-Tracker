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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import club.ourail.cumtdtracker.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    class Stop(
        var code: String,
        var stopid: String,
        var stopname: String,
        var stopdistance: Double,
        var ratio: Int
    )


    var lat = -9999.99
    var lon = -9999.99
    var isNightMode = true
    private val url = "https://developer.cumtd.com/"//
    private var key = "e6a7c2b0bdb741569cc69a1505a6c08e"//

    private var stops = mutableListOf<Stop>()

    private lateinit var warning: TextView
    private lateinit var listview: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var adapter: RecyclerView.Adapter<StopHolder>
    private lateinit var manager: RecyclerView.LayoutManager
    private lateinit var appBarLayout: com.google.android.material.appbar.MaterialToolbar

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var pager: ViewPager
    private lateinit var tab: BottomNavigationView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var fragment1: ListFragment
    private lateinit var fragment2: MapFragment
    private lateinit var fragment3: SaferideFragment


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val p = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        this.window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarColor)
        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                isNightMode = false
            }
        }

        appBarLayout = findViewById(R.id.appbar_layout)
        setSupportActionBar(appBarLayout)

//        fab = findViewById(R.id.fab_main)
//        fab2 = findViewById(R.id.fab_main2)
//        fab2.setImageResource(
//            if (p.getBoolean(
//                    "isList",
//                    true
//                )
//            ) R.drawable.ic_map else R.drawable.ic_list
//        )
//        fab2.setOnClickListener {
//            pager.currentItem = if (pager.currentItem == 1) 0 else 1
//        }


        fragment1 = ListFragment()
        fragment2 = MapFragment()
//        fragment3 = SaferideFragment()
        if (savedInstanceState == null) {
            fragment1.retainInstance = true
            fragment2.retainInstance = true
//            fragment3.retainInstance = true
        }

        pager = findViewById(R.id.view_pager)
        pager.adapter = object : FragmentPagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> fragment1
//                    1 -> fragment3
                    1 -> fragment2
                    else -> fragment1
                }
            }

            override fun getCount(): Int = 2
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    bottomNavigationView.selectedItemId = R.id.navigation_list
                }
//                bottomNavigationView.selectedItemId = when (position) {
//                    0 -> R.id.navigation_list
//                    2 -> R.id.navigation_map
//                    else ->
//                }

                if (position == 1) {
                    bottomNavigationView.selectedItemId = R.id.navigation_map
                }
            }
        })

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_list -> {
                    pager.currentItem = 0
                    true
                }
                R.id.navigation_map -> {
                    pager.currentItem = 2
                    true
                }
                else -> false
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        warning = findViewById(R.id.warning_main)

        checkPermission()
    }


    private fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.smallestDisplacement = 50f // 170 m = 0.1 mile
        locationRequest.priority =
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationCallback = LocationCallback()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* loop */
        )

    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        supportFragmentManager.popBackStackImmediate()
//        stopLocationUpdates()
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
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    swipeLayout.isRefreshing = false
                    warning.text = "Permission denied.\nPlease enable location manually."
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
