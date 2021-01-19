package club.ourail.cumtdtracker

data class StopResponse(
    var time: String,
    var stops: List<Stop>,
    var status: Status
)

data class Stop(
    var stop_id: String,
    var stop_name: String,
    var code: String,
    var distance: Float,
    var stop_points: List<StopPoint>
)

data class StopPoint(
    var stop_id: String,
    var stop_lat: Float,
    var stop_lon: Float,
    var stop_name: String
)

// Departures
data class BusResponse(
    var departures: List<Bus>,
    var status: Status
)

data class Bus(
    var headsign: String,
    var route: Route,
    var trip: Trip,
    var expected_mins: Int,
    var is_istop: Boolean,
    var is_monitored: Boolean
)

data class Route(
    var route_color: String,
    var route_short_name: String,
    var route_long_name: String,
    var route_text_color: String
)

data class Trip(
    val trip_id: String,
    var trip_headsign: String,
    var direction: String
)

data class Status(
    val code: Int,
    val msg: String
)

// Trip Stops
data class TripResponse(
    val stop_times: ArrayList<TripStop>,
    val status:Status
)

data class TripStop(
    var departure_time: String,
    var stop_point: StopPoint
)

data class AllStopsResponse(
    val changeset_id: String,
    val new_changeset: Boolean,
    val status: Status,
    val stops: List<Stop>,
    val time: String
)