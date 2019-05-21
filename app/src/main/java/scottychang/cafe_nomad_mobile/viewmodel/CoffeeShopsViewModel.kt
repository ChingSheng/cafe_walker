package scottychang.cafe_nomad_mobile.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import scottychang.cafe_nomad_mobile.MyCallback
import scottychang.cafe_nomad_mobile.model.CoffeeShop
import scottychang.cafe_nomad_mobile.model.LatLng
import scottychang.cafe_nomad_mobile.model.TwCity
import scottychang.cafe_nomad_mobile.repositiory.CoffeeShopRepository
import scottychang.cafe_nomad_mobile.repositiory.PositioningRepository
import scottychang.cafe_nomad_mobile.repositiory.SharePrefRepository
import java.util.*

class CoffeeShopsViewModel(application: Application) : AndroidViewModel(application) {
    var coffeeShops = MutableLiveData<List<Pair<CoffeeShop, Double>>>()
    var exceptions = MutableLiveData<Exception>()
    var loading = MutableLiveData<Boolean>()

    var twCity : TwCity = TwCity.UNKNOWN
    private set
    var current = ArrayList<CoffeeShop>()
    private set

    init {
        var city = SharePrefRepository.getInstance().load(getApplication())
        if (city == TwCity.UNKNOWN) city = PositioningRepository.getNearestCity(application)
        setCoffeeShopsCity(city)
    }

    fun setCoffeeShopsCity(twCity: TwCity) {
        this.twCity = twCity
        SharePrefRepository.getInstance().save(getApplication(), twCity)

        loading.postValue(true)
        CoffeeShopRepository.getInstance(getApplication())
            .loadCoffeeShops(twCity.type, object : MyCallback<List<CoffeeShop>> {
                override fun onFailure(exception: Exception) {
                    loading.postValue(false)
                    exceptions.postValue(exception)
                    coffeeShops.postValue(Collections.emptyList())
                }

                override fun onSuccess(result: List<CoffeeShop>) {
                    loading.postValue(false)
                    if (current != result) {
                        current.clear()
                        current.addAll(result)
                    }
                    updateNearestByLatLng(PositioningRepository.loadLatlng(getApplication()))
                }
            })
    }

    fun updateNearestByLatLng (latlng:LatLng) {
        val pairs = current.associate { item -> Pair(item, PositioningRepository.getDistance(latlng, LatLng(item.latitude?.toDouble()?:.0, item.longitude?.toDouble()?:.0))) }
        coffeeShops.postValue(pairs.toList().sortedBy { (_, value) -> value }.subList(0, Math.min(50, pairs.size)))
    }
}