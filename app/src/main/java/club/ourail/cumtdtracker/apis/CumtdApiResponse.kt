package club.ourail.cumtdtracker

import com.google.gson.annotations.SerializedName

class StopResponse {
    @SerializedName("time")
    var time: String? = null

    @SerializedName("stops")
    var stops = ArrayList<Stop>()

    @SerializedName("status")
    var status: Status? = null
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

// Departures
class BusResponse {
    @SerializedName("departures")
    var Buses = ArrayList<Bus>()

    @SerializedName("status")
    var status: Status? = null
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

    @SerializedName("is_monitored")
    var IsMonitored: Boolean? = null
}

class Route {
    @SerializedName("route_color")
    var Color: String? = null

    @SerializedName("route_short_name")
    var ShortName: String? = null

    @SerializedName("route_long_name")
    var LongName: String? = null

    @SerializedName("route_text_color")
    var ColorText: String? = null
}

class Trip {
    @SerializedName("trip_id")
    val TripId: String? = null

    @SerializedName("trip_headsign")
    var TripHeadsign: String? = null

    @SerializedName("direction")
    var Direction: String? = null
}

class Status {
    @SerializedName("code")
    var code: Int? = null

    @SerializedName("msg")
    var msg: String? = null
}

// Trip Stops
class TripResponse {
    @SerializedName("stop_times")
    var StopTimes = ArrayList<TripStop>()
}

class TripStop {
    @SerializedName("departure_time")
    var DepartureTime:String? = null

    @SerializedName("stop_point")
    var StopPoint: StopPoint? = null
}
