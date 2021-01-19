data class stopList(
    val changeset_id: String,
    val new_changeset: Boolean,
    val rqst: Rqst,
    val status: Status,
    val stops: List<Stop>,
    val time: String
)