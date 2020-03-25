package club.ourail.cumtdtracker

import com.google.gson.annotations.SerializedName

class StopResponse {
    @SerializedName("time")
    var time: String? = null

    @SerializedName("stops")
    var stops = ArrayList<Stop>()

}

class Stop {
    @SerializedName("stop_id")
    var StopId: String? = null

    @SerializedName("stop_name")
    var StopName: String? = null

    @SerializedName("code")
    var StopCode: String? = null

    @SerializedName("distance")
    var StopDistance: Float? = null;

    @SerializedName("stop_points")
    var StopPoints = ArrayList<StopPoint>()
}

class StopPoint {
    @SerializedName("stop_id")
    var StopId: String? = null

    @SerializedName("stop_lat")
    var StopLat: Float? = null

    @SerializedName("stop_lon")
    var StopLon: Float? = null

    @SerializedName("stop_name")
    var StopName: String? = null
}

//
// Parsing Departures
//
class BusResponse{
    @SerializedName("departures")
    var Buses = ArrayList<Bus>()
}

class Bus {
    @SerializedName("headsign")
    var HeadSign: String? = null

    @SerializedName("route")
    var route: Route? = null

    @SerializedName("trip")
    var trip: Trip? = null

    @SerializedName("expected_mins")
    var ExpectedMins: Int? = null

    @SerializedName("is_istop")
    var IsIstop: Boolean? = null
}

class Route {
    @SerializedName("route_color")
    var Color: String? = null
}

class Trip {
    @SerializedName("trip_headsign")
    var TripHeadsign: String? = null
}

