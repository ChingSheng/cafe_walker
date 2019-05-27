package scottychang.cafe_nomad_mobile.repositiory

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import scottychang.cafe_nomad_mobile.model.LatLng

class PositioningRepository() {
    companion object {
        // Taipei
        var defaultLat = 25.09108
        var defaultLng = 121.5598

        @SuppressLint("MissingPermission")
        fun loadLatLng(context: Context): LatLng {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var bestLocation: android.location.Location? = null
            for (provider in lm.allProviders) {
                val location = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || location.accuracy > bestLocation.accuracy) {
                    bestLocation = location
                }
            }
            return LatLng(bestLocation?.latitude ?: defaultLat, bestLocation?.longitude ?: defaultLng)
        }
    }
}