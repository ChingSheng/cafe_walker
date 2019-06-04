package scottychang.cafe_walker.repositiory

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import scottychang.cafe_walker.model.LatLng

class PositioningRepository() {
    companion object {
        private val MIN_UPDATE_TIME_MS = 3000L
        private val MIN_DISTANCE_METER = 3f

        // Taipei
        private val defaultLat = 25.09108
        private val defaultLng = 121.5598

        private var hasRegister = false
        var bestLocation: android.location.Location? = null

        @SuppressLint("MissingPermission")
        fun loadLatLng(context: Context): LatLng {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if(!hasRegister) {
                val bestProvider= findBestProvider(context)
                if (!bestProvider.isEmpty()) {
                    lm.requestLocationUpdates(bestProvider, MIN_UPDATE_TIME_MS, MIN_DISTANCE_METER, locationListener)
                    bestLocation = lm.getLastKnownLocation(bestProvider)
                }
                hasRegister = true
            }
            return LatLng(bestLocation?.latitude ?: defaultLat, bestLocation?.longitude ?: defaultLng)
        }

        @SuppressLint("MissingPermission")
        private fun findBestProvider(context:Context): String {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE;
            criteria.isAltitudeRequired = false;
            criteria.isBearingRequired = false;
            criteria.isCostAllowed = true;
            criteria.powerRequirement = Criteria.POWER_LOW;
            return lm.getBestProvider(criteria, true)
        }
        
        private val locationListener = object :LocationListener {
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

            override fun onProviderEnabled(p0: String?) {}

            override fun onProviderDisabled(p0: String?) {}

            override fun onLocationChanged(newLocation: Location?) {
                bestLocation = newLocation!!
            }
        }
    }
}