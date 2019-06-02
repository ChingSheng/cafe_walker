package scottychang.cafe_nomad_mobile.repositiory

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import scottychang.cafe_nomad_mobile.model.LatLng

class PositioningRepository() {
    companion object {
        // Taipei
        var defaultLat = 25.09108
        var defaultLng = 121.5598

        var bestLocation: android.location.Location? = null
        var hasRegister = false

        @SuppressLint("MissingPermission")
        fun loadLatLng(context: Context): LatLng {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if(!hasRegister) {
                val bestProvider= findBestProvider(context)
                if (!bestProvider.isEmpty()) {
                    lm.requestLocationUpdates(bestProvider, 0, .5f, locationListener)
                    bestLocation = lm.getLastKnownLocation(bestProvider)
                }
                hasRegister = true
            }
            return LatLng(bestLocation?.latitude ?: defaultLat, bestLocation?.longitude ?: defaultLng)
        }

        @SuppressLint("MissingPermission")
        private fun findBestProvider(context:Context): String {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var accuracy = Float.MAX_VALUE
            var result:String = ""
            for (provider in lm.allProviders) {
                val location = lm.getLastKnownLocation(provider) ?: continue
                Log.d("DADA2", location.provider +  " " + location.latitude + " " + location.longitude +  " " + location.accuracy )
                if (location.accuracy < accuracy) {
                    accuracy = location.accuracy
                    result = provider
                }

            }
            Log.d("DADA", "bestprovider:" + result)
            return result
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