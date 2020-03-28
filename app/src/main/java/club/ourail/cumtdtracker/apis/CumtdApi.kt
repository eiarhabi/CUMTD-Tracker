package club.ourail.cumtdtracker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface getstopsbylatlon {
    @GET("api/v2.2/json/getstopsbylatlon?")
    fun getStopByLatLon(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("key") apikey: String
    ): Call<StopResponse>
}

interface getdeparturesbystop {
    @GET("api/v2.2/json/getdeparturesbystop?")
    fun getDeparturesByStop(
        @Query("stop_id") stopId: String,
        @Query("pt") time: Int,
        @Query("key") apikey: String
    ): Call<BusResponse>
}

interface getstoptimesbytrip {
    @GET("api/v2.2/json/getstoptimesbytrip?")
    fun getStopTimeByTrip(
        @Query("trip_id") tripId: String,
        @Query("key") apikey: String
    ): Call<TripResponse>

}