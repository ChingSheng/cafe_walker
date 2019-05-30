package scottychang.cafe_nomad_mobile.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import org.osmdroid.config.Configuration
import scottychang.cafe_nomad_mobile.BuildConfig
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.adapter.CoffeeShopsSimpleListAdapter
import scottychang.cafe_nomad_mobile.data.CityString
import scottychang.cafe_nomad_mobile.model.LatLng
import scottychang.cafe_nomad_mobile.model.TwCity
import scottychang.cafe_nomad_mobile.viewmodel.CoffeeShopsViewModel
import scottychang.cafe_nomad_mobile.viewmodel.PositioningViewModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val MAX_ZOOM_IN_LEVEL = 20.0f
    private val MIN_ZOOM_IN_LEVEL = 11.0f
    private val DEFAULT_ZOOM_IN_LEVEL = 16.5f

    private val container: FrameLayout by bindView(R.id.container)
    private val itemsView: RecyclerView by bindView(R.id.items)
    private val floatingButton: FloatingActionButton by bindView(R.id.floating_button)
    private val loading: FrameLayout by bindView(R.id.loading)

    private lateinit var mapFragment: MapFragment
    private lateinit var coffeeShopsViewModel: CoffeeShopsViewModel
    private lateinit var positioningViewModel: PositioningViewModel

    private lateinit var map:GoogleMap

    companion object {
        private val SHOW_INTRODUCTION_DIALOG = "show_dialog"
        fun go(context: Context, showIntroductionDialog: Boolean) {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra(SHOW_INTRODUCTION_DIALOG, showIntroductionDialog)
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
        showIntroductionDialogIfNeeded()

        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    private fun showIntroductionDialogIfNeeded() {
        val shouldShowDialog = intent?.getBooleanExtra(SHOW_INTRODUCTION_DIALOG, false)
        shouldShowDialog?.let { showDialog ->
            if (showDialog) {
                val builder = AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog)
                builder.setMessage(R.string.first_launch_msg)
                builder.setPositiveButton(R.string.first_launch_button_text, null)
                builder.create().show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.isMyLocationEnabled = true
        map.setMaxZoomPreference(MAX_ZOOM_IN_LEVEL)
        map.setMinZoomPreference(MIN_ZOOM_IN_LEVEL)

        initPositioning()
        initCoffeeShops()
        initFloatingButton()
    }

    private fun initPositioning() {
        positioningViewModel = ViewModelProviders.of(this).get(PositioningViewModel::class.java)
        positioningViewModel.latLng.observe(this, Observer<LatLng> { latLng -> setCenter(latLng) })
    }

    private fun setCenter(t: LatLng?) {
        val s : com.google.android.gms.maps.model.LatLng = com.google.android.gms.maps.model.LatLng(t!!.latitude, t!!.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(s, DEFAULT_ZOOM_IN_LEVEL));
    }

    private fun initCoffeeShops() {
        coffeeShopsViewModel = ViewModelProviders.of(this).get(CoffeeShopsViewModel::class.java)
        coffeeShopsViewModel.coffeeShops.observe(this, Observer { setupViewData() })
        coffeeShopsViewModel.exceptions.observe(
            this,
            Observer { Toast.makeText(this, it?.message ?: "UnknownError", Toast.LENGTH_LONG).show() })
        coffeeShopsViewModel.loading.observe(
            this,
            Observer { isLoading -> loading.visibility = if (isLoading!!) View.VISIBLE else View.GONE })
    }

    private fun setupViewData() {
        itemsView.layoutManager = LinearLayoutManager(this)
        itemsView.adapter = CoffeeShopsSimpleListAdapter(
            getString(CityString.data.get(coffeeShopsViewModel.twCity) ?: R.string.unknown_location),
            coffeeShopsViewModel.getDistancePairFromPosition(positioningViewModel.latLng.value!!)
        ) { id: String? -> id?.let { ShopDetailActivity.go(this, it) } }
        (itemsView.adapter as CoffeeShopsSimpleListAdapter).setSlideChangeListener { distance: Float -> translationY(distance) }
    }

    private fun initFloatingButton() {
        floatingButton.setOnClickListener {
            positioningViewModel.reloadFromGps()
            updateViewData()
        }
        floatingButton.setOnLongClickListener { createPopupMenu(it) }
    }

    private fun updateViewData() {
        val coffeeShopPair = coffeeShopsViewModel.getDistancePairFromPosition(positioningViewModel.latLng.value!!)
        itemsView.adapter?.let {
            (it as CoffeeShopsSimpleListAdapter).updateData(coffeeShopPair)
        }
    }

    private fun translationY(distance: Float) {
        if (distance > 0) {
            container.translationY = - distance * resources.displayMetrics.density * 0.7f
        }
    }

    //================================================================================
    // Menu
    //================================================================================

    private fun createPopupMenu(view: View?):Boolean {
        val popupMenu = PopupMenu(this, view!!)
        popupMenu.menuInflater.inflate(R.menu.map_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.location_north).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_middle).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_south).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_east).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_isolated).subMenu.clearHeader()
        popupMenu.setOnMenuItemClickListener { view -> this.onMenuItemSelected(view!!) }
        popupMenu.show()
        return true
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean  {
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
        }?.let { city: TwCity ->
            coffeeShopsViewModel.setCoffeeShopsCity(city)
        }
        return true
    }
}