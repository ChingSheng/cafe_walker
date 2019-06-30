package scottychang.cafe_walker.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import scottychang.cafe_walker.data.CityLatLng
import scottychang.cafe_walker.model.CoffeeShop
import scottychang.cafe_walker.model.LatLng
import scottychang.cafe_walker.model.TwCity
import scottychang.cafe_walker.repositiory.CoffeeShopRepository
import scottychang.cafe_walker.repositiory.MyCallback
import scottychang.cafe_walker.repositiory.PositioningRepository
import scottychang.cafe_walker.repositiory.SharePrefRepository
import java.util.*
import kotlin.collections.HashMap

class CoffeeShopsViewModel(application: Application) : AndroidViewModel(application) {
    private val MAX_SHOPS_IN_BOTTOM_SHEET = 50

    var coffeeShops = MutableLiveData<List<CoffeeShop>>()
    var exceptions = MutableLiveData<Exception>()
    var loading = MutableLiveData<Boolean>()

    var twCity: TwCity = TwCity.UNKNOWN
        private set
    var current: Map<String, CoffeeShop> = HashMap()
        private set

    init {
        var city = SharePrefRepository.getInstance().loadCity(getApplication())
        if (city == TwCity.UNKNOWN) city = getNearestCity(application)
        setCoffeeShopsCity(city)
    }

    fun setCoffeeShopsCity(twCity: TwCity) {
        this.twCity = twCity
        SharePrefRepository.getInstance().saveCity(getApplication(), twCity)

        loading.postValue(true)
        CoffeeShopRepository.getInstance(getApplication())
            .loadCoffeeShops(twCity.type, object :
                MyCallback<List<CoffeeShop>> {
                override fun onFailure(exception: Exception) {
                    loading.postValue(false)
                    exceptions.postValue(exception)
                    coffeeShops.postValue(Collections.emptyList())
                }

                override fun onSuccess(result: List<CoffeeShop>) {
                    loading.postValue(false)
                    current = result.associate { it.id to it }.toMap()
                    coffeeShops.postValue(result)
                }
            })
    }

    fun getDistancePairFromPosition(position: LatLng): List<Pair<CoffeeShop, Double>> {
        val coffeeDistancePair = current.map { item ->
            Pair(
                item.value,
                getDistance(position, getLatLng(item.value))
            )
        }
        val result = coffeeDistancePair.toList().sortedBy { (_, distance) -> distance }.subList(
            0,
            Math.min(MAX_SHOPS_IN_BOTTOM_SHEET, coffeeDistancePair.size)
        )
        return result
    }

    private fun getLatLng(coffeeShop: CoffeeShop): LatLng {
        return LatLng(coffeeShop.latitude?.toDouble() ?: .0, coffeeShop.longitude?.toDouble() ?: .0)
    }

    fun getNearestCity(context: Context): TwCity {
        val current = PositioningRepository.loadLatLng(context)
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

    private fun getDistance(point1: LatLng, point2: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, result)
        return result.get(0).toDouble()
    }
}