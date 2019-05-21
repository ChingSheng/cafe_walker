package scottychang.cafe_nomad_mobile.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import scottychang.cafe_nomad_mobile.BuildConfig
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.adapter.CoffeeShopsSimpleListAdapter
import scottychang.cafe_nomad_mobile.data.CityString
import scottychang.cafe_nomad_mobile.model.LatLng
import scottychang.cafe_nomad_mobile.model.TwCity
import scottychang.cafe_nomad_mobile.viewmodel.CoffeeShopsViewModel
import scottychang.cafe_nomad_mobile.viewmodel.PositioningViewModel

class MapActivity : AppCompatActivity() {
    private val MAX_ZOOM_IN_LEVEL = 20.0
    private val MIN_ZOOM_IN_LEVEL = 11.0
    private val DEFAULT_ZOOM_IN_LEVEL = 16.0

    private val mapView: MapView by bindView(R.id.map)
    private val mapController: IMapController by lazy { mapView.controller }
    private val itemsView: RecyclerView by bindView(R.id.items)

    private val floatingButton: FloatingActionButton by bindView(R.id.floating_button)
    private val loading: FrameLayout by bindView(R.id.loading)

    private lateinit var coffeeShopsViewModel: CoffeeShopsViewModel
    private lateinit var positioningViewModel: PositioningViewModel

    companion object {
        fun go(context: Context) {
            val intent = Intent(context, MapActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun <T : View> bindView(@IdRes resId: Int): Lazy<T> = lazy { findViewById<T>(resId) }

    init {
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        initMapZoomInSpec()
        initMapTileSource()

        positioningViewModel = ViewModelProviders.of(this).get(PositioningViewModel::class.java)
        positioningViewModel.latLng.observe(this, Observer<LatLng> { latLng -> setCenter(latLng) })

        coffeeShopsViewModel = ViewModelProviders.of(this).get(CoffeeShopsViewModel::class.java)
        coffeeShopsViewModel.coffeeShops.observe(this, Observer { initRecyclerView() })
        coffeeShopsViewModel.exceptions.observe(this, Observer { Toast.makeText(this, it?.message ?: "UnknownError", Toast.LENGTH_LONG).show() })
        coffeeShopsViewModel.loading.observe(this, Observer { isLoading -> loading.visibility = if (isLoading!!) View.VISIBLE else View.GONE })

        floatingButton.setOnClickListener { positioningViewModel.reloadFromGps() }
        floatingButton.setOnLongClickListener { createPopupMenu(it) }
    }

    private fun createPopupMenu(view: View?):Boolean {
        val popupMenu = PopupMenu(this@MapActivity, view!!)
        popupMenu.menuInflater.inflate(R.menu.map_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.location_north).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_middle).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_south).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_east).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_isolated).subMenu.clearHeader()
        popupMenu.setOnMenuItemClickListener { view -> this@MapActivity.onMenuItemSelected(view!!) }
        popupMenu.show()
        return true
    }

    private fun setCenter(t: LatLng?) {
        val startPoint = GeoPoint(t?.latitude ?: .0, t?.longitude ?: .0)
        mapController.setZoom(DEFAULT_ZOOM_IN_LEVEL)
        mapController.setCenter(startPoint)
    }

    private fun initRecyclerView() {
        itemsView.layoutManager = LinearLayoutManager(this)
        itemsView.adapter = CoffeeShopsSimpleListAdapter(
            getString(CityString.data.get(coffeeShopsViewModel.twCity) ?: R.string.unknown_location),
            coffeeShopsViewModel.coffeeShops.value,
            { position: Int -> focusByModelPosition(position) })
    }

    private fun focusByModelPosition(position: Int) {
        val coffeeShop = coffeeShopsViewModel.coffeeShops.value?.get(position)
        val startPoint = GeoPoint(coffeeShop?.first?.latitude?.toDouble() ?: .0, coffeeShop?.first?.longitude?.toDouble() ?: .0)
        mapController.setCenter(startPoint)
    }

    private fun initMapTileSource() {
        val scale = baseContext.resources.displayMetrics.density
        val newScale = (128 * scale).toInt()
        val OSMSource = arrayOfNulls<String>(2)
        OSMSource[0] = "http://a.tile.openstreetmap.org/"
        OSMSource[1] = "http://b.tile.openstreetmap.org/"
        val MapSource =
            XYTileSource("OSM", MIN_ZOOM_IN_LEVEL.toInt(), MAX_ZOOM_IN_LEVEL.toInt(), newScale, ".png", OSMSource)
        mapView.setTileSource(MapSource)
    }

    private fun initMapZoomInSpec() {
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapView.setMaxZoomLevel(MAX_ZOOM_IN_LEVEL)
        mapView.setMinZoomLevel(MIN_ZOOM_IN_LEVEL)
        mapController.setZoom(DEFAULT_ZOOM_IN_LEVEL)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    //================================================================================
    // Menu
    //================================================================================

    fun onMenuItemSelected(item: MenuItem): Boolean  {
        when (item.itemId) {
            R.id.taipei -> TwCity.TAIPEI
            R.id.keelung -> TwCity.KEELUNG
            R.id.taoyuan -> TwCity.TAOYUAN
            R.id.hsinchu -> TwCity.HSINCHU
            R.id.miaoli -> TwCity.MIAOLI
            R.id.taichung -> TwCity.TAICHUNG
            R.id.changhua -> TwCity.CHANGHUA
            R.id.yunlin -> TwCity.YUNLIN
            R.id.nantou -> TwCity.NANTOU
            R.id.chiayi -> TwCity.CHIAYI
            R.id.tainan -> TwCity.TAINAN
            R.id.kaohsiung -> TwCity.KAOHSIUNG
            R.id.pingtung -> TwCity.PINGTUNG
            R.id.yilan -> TwCity.YILAN
            R.id.hualien -> TwCity.HUALIEN
            R.id.taitung -> TwCity.TAITUNG
            R.id.penghu -> TwCity.PENGHU
            R.id.lienchiang -> TwCity.LIENCHIANG
            else -> null
        }?.let {
            coffeeShopsViewModel.setCoffeeShopsCity(it)
        }
        return true
    }
}