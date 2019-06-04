package scottychang.cafe_walker.model

data class CoffeeShop (
    val id: String,
    val name: String,
    val city: String,

    val music: Double?,
    val cheap: Double?,
    val tasty: Double?,
    val quiet: Double?,
    val seat: Double?,
    val wifi: Double?,

    val url: String?,
    val address: String?,
    val latitude: String?,
    val limited_time: String?,
    val longitude: String?,

    val mrt: String?,
    val open_time: String?,
    val socket: String?,
    val standing_desk: String?
)
