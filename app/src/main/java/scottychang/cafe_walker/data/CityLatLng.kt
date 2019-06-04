package scottychang.cafe_walker.data

import scottychang.cafe_walker.model.LatLng
import scottychang.cafe_walker.model.TwCity
/* From https://byronhu.wordpress.com/2013/09/09/%E5%8F%B0%E7%81%A3%E7%B8%A3%E5%B8%82%E7%B6%93%E7%B7%AF%E5%BA%A6/ */
class CityLatLng {
    companion object {
        val data = mutableMapOf(
            TwCity.TAIPEI to LatLng(25.09108, 121.5598),
            TwCity.KEELUNG to LatLng(25.10898, 121.7081),
            TwCity.TAOYUAN to LatLng(24.93759, 121.2168),
            TwCity.HSINCHU to LatLng(24.80395, 120.9647),
            TwCity.MIAOLI to LatLng(24.48927, 120.9417),
            TwCity.TAICHUNG to LatLng(24.23321, 120.9417),
            TwCity.CHANGHUA to LatLng(23.99297, 120.4818),
            TwCity.NANTOU to LatLng(23.83876, 120.9876),
            TwCity.YUNLIN to LatLng(23.75585, 120.3897),
            TwCity.CHIAYI to LatLng(23.47545, 120.4473),
            TwCity.TAINAN to LatLng(23.1417, 120.2513),
            TwCity.KAOHSIUNG to LatLng(23.01087, 120.666),
            TwCity.PINGTUNG to LatLng(22.54951, 120.62),
            TwCity.YILAN to LatLng(24.69295, 121.7195),
            TwCity.HUALIEN to LatLng(23.7569, 121.3542),
            TwCity.TAITUNG to LatLng(22.98461, 120.9876),
            TwCity.PENGHU to LatLng(23.56548, 119.6151),
            TwCity.LIENCHIANG to LatLng(26.19737, 119.5397)
        )
    }
}