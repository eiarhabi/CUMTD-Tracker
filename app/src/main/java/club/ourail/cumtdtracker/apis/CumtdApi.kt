package club.ourail.cumtdtracker

import retrofit2.*
import retrofit2.http.*

interface getstopsbylatlon {
    @GET("api/v2.2/json/getstopsbylatlon?")
    fun getStopByLatLon(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("key") apikey: String): Call<StopResponse>
}

interface getdeparturesbystop {
    @GET("api/v2.2/json/getdeparturesbystop?")
    fun getDeparturesByStop(
        @Query("stop_id") stopId: String,
        @Query("pt") time: Int,
        @Query("key") apikey: String): Call<BusResponse>
}