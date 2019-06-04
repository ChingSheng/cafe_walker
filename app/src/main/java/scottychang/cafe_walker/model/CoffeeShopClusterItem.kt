package scottychang.cafe_walker.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class CoffeeShopClusterItem(private val coffeeShop: CoffeeShop): ClusterItem {
    override fun getSnippet(): String {
        return coffeeShop.address?: ""
    }

    override fun getTitle(): String {
        return coffeeShop.name
    }

    override fun getPosition(): LatLng {
        return LatLng(coffeeShop.latitude!!.toDouble(), coffeeShop.longitude!!.toDouble())
    }

    fun getId(): String {
        return coffeeShop.id
    }
}