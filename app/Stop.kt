data class Stop(
    val code: String,
    val distance: Double,
    val stop_id: String,
    val stop_name: String,
    val stop_points: List<StopPoint>
)