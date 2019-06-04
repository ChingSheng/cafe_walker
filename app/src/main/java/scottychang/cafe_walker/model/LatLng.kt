package scottychang.cafe_walker.model

data class LatLng(private val _latitude: Double, private val _longitude: Double) {
    val latitude: Double
        get() = if (_latitude >= 0) _latitude else .0
    val longitude: Double
        get() = if (_longitude >=0) _longitude else .0
}
