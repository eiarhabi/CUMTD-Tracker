package club.ourail.cumtdtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import club.ourail.cumtdtracker.MainActivity.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import java.io.File


@Parcelize
data class S(
    val id: String,
    var name: String,
    var nameStopPoint: String,
    val lat: Float,
    val lon: Float
) : Parcelable


class MapFragment : Fragment() {
    private var list = mutableListOf<S>()
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isStart = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_main, container, false)
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val ac = activity as MainActivity

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (list.isNullOrEmpty()) {
            val file = File(context?.filesDir, "getstops.json")
            val a = GsonBuilder().create().fromJson(file.readText(), AllStopsResponse::class.java) as AllStopsResponse

            for (i in a.stops) {
                for (j in i.stop_points)
                    list.add(
                        S(
                            i.stop_id,
                            i.stop_name,
                            j.stop_name,
                            j.stop_lat,
                            j.stop_lon
                        )
                    )
            }
        }



        mapView = ac.findViewById(R.id.map_view_main)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync {
            map = it
            map.isMyLocationEnabled = true

            if (ac.isNightMode)
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        activity,
                        R.raw.map_color_dark
                    )
                )

            for (i in list) {
                map.addMarker(
                    MarkerOptions().position(LatLng(i.lat.toDouble(), i.lon.toDouble()))
                        .title(i.nameStopPoint).visible(true)
                )
            }

            map.setOnInfoWindowClickListener { marker ->
                var id: String? = null
                var name: String? = null
                for (i in list) {
                    if (i.nameStopPoint == marker.title) {
                        id = i.id
                        name = i.name
                    }
                }
                val intent = Intent(view?.context, StopActivity::class.java)
                intent.putExtra("title", name)
                intent.putExtra("stopid", id)
                ac.startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun getLocation() {
        FusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener { location ->
            if (location != null)
                mapView.getMapAsync { map ->
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            17f
                        )
                    )
                }
        }
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