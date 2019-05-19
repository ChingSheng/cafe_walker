package scottychang.cafe_nomad_mobile.repositiory

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import scottychang.cafe_nomad_mobile.data.CityLatLng
import scottychang.cafe_nomad_mobile.model.LatLng
import scottychang.cafe_nomad_mobile.model.TwCity

class PositioningRepository() {
    companion object {
        // Taipei
        var defaultLat = 25.09108
        var defaultLng = 121.5598

        @SuppressLint("MissingPermission")
        fun loadLatlng(context: Context): LatLng {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var bestLocation: android.location.Location? = null
            for (provider in lm.allProviders) {
                val location = lm.getLastKnownLocation(provider)
                if (location == null) continue
                if (bestLocation == null || location.accuracy > bestLocation.accuracy) {
                    bestLocation = location
                }
            }
            return LatLng(bestLocation?.latitude ?: defaultLat, bestLocation?.longitude ?: defaultLng)
        }

        fun getNearestCity(context: Context): TwCity {
            val current = loadLatlng(context)
            var bestDistance = -.1
            var city: TwCity = TwCity.UNKNOWN
            for ((twCity, latlng) in CityLatLng.data) {
                val distance = getDistance(latlng, current)
                if (bestDistance < 0 || distance < bestDistance) {
                    bestDistance = distance
                    city = twCity
                }
            }
            return city
        }

        fun getDistance(point1: LatLng, point2: LatLng): Double {
            val x = FloatArray(1)
            Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, x)
            return x.get(0).toDouble()
        }
    }
}