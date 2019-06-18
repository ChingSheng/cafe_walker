package scottychang.cafe_walker.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import scottychang.cafe_walker.R
import scottychang.cafe_walker.adapter.CoffeeShopsSimpleListAdapter
import scottychang.cafe_walker.data.CityString
import scottychang.cafe_walker.model.CoffeeShopClusterItem
import scottychang.cafe_walker.model.LatLng
import scottychang.cafe_walker.model.TwCity
import scottychang.cafe_walker.viewmodel.CoffeeShopsViewModel
import scottychang.cafe_walker.viewmodel.PositioningViewModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val MAX_ZOOM_IN_LEVEL = 20.0f
    private val MIN_ZOOM_IN_LEVEL = 11.0f
    private val DEFAULT_ZOOM_IN_LEVEL = 16.5f

    private val container: FrameLayout by bindView(R.id.container)
    private val recyclerView: RecyclerView by bindView(R.id.recycler_view)
    private val bottomSheet: ConstraintLayout by bindView(R.id.items)
    private val bottomSheetTitleItem: FrameLayout by bindView(R.id.title_item)
    private val bottomSheetTitle: TextView by bindView(R.id.city)
    private val bottomSheetIndicator: ImageView by bindView(R.id.indicator)
    private val floatingButton: FloatingActionButton by bindView(R.id.floating_button)
    private val loading: FrameLayout by bindView(R.id.loading)

    private lateinit var mapFragment: MapFragment
    private lateinit var coffeeShopsViewModel: CoffeeShopsViewModel
    private lateinit var positioningViewModel: PositioningViewModel

    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<CoffeeShopClusterItem>

    companion object {
        private const val SHOW_INTRODUCTION_DIALOG = "show_dialog"
        fun go(context: Context, showIntroductionDialog: Boolean) {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra(SHOW_INTRODUCTION_DIALOG, showIntroductionDialog)
            context.startActivity(intent)
        }
    }

    fun <T : View> bindView(@IdRes resId: Int): Lazy<T> = lazy { findViewById<T>(resId) }

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
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setMaxZoomPreference(MAX_ZOOM_IN_LEVEL)
        map.setMinZoomPreference(MIN_ZOOM_IN_LEVEL)

        clusterManager = ClusterManager(this, map)
        clusterManager.setOnClusterClickListener { zoomInFromCluster(it) }
        clusterManager.setOnClusterItemInfoWindowClickListener { ShopDetailActivity.go(this, it!!.getId()) }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)

        initPositioning()
        initCoffeeShops()
        initFloatingButton()
    }

    private fun zoomInFromCluster(it: Cluster<CoffeeShopClusterItem>): Boolean {
        val cameraPosition = CameraPosition.builder().target(it.position).zoom(map.cameraPosition.zoom + 2).build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        return true
    }

    private fun initPositioning() {
        positioningViewModel = ViewModelProviders.of(this).get(PositioningViewModel::class.java)
        positioningViewModel.latLng.observe(this, Observer<LatLng> { latLng -> setCenter(latLng) })
    }

    private fun setCenter(position: LatLng?) {
        val s: com.google.android.gms.maps.model.LatLng =
            com.google.android.gms.maps.model.LatLng(position!!.latitude, position!!.longitude)
        val cameraPosition = CameraPosition.builder().target(s).zoom(DEFAULT_ZOOM_IN_LEVEL).build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun initCoffeeShops() {
        coffeeShopsViewModel = ViewModelProviders.of(this).get(CoffeeShopsViewModel::class.java)
        coffeeShopsViewModel.coffeeShops.observe(this, Observer { setupViewAndData() })
        coffeeShopsViewModel.exceptions.observe(
            this,
            Observer { Toast.makeText(this, it?.message ?: "UnknownError", Toast.LENGTH_LONG).show() })
        coffeeShopsViewModel.loading.observe(
            this,
            Observer { isLoading -> loading.visibility = if (isLoading!!) View.VISIBLE else View.GONE })
    }

    private fun setupViewAndData() {
        setupMapViewCluster()
        setupBottomSheetTitle()
        setupBottomSheetData()
    }

    private fun setupMapViewCluster() {
        map.clear()
        clusterManager.clearItems()
        clusterManager.addItems(coffeeShopsViewModel.coffeeShops.value!!.map { CoffeeShopClusterItem(it) })
        clusterManager.cluster()
    }

    private fun setupBottomSheetTitle() {
        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(bottomSheetBehaviorCallback)
        bottomSheetTitleItem.setOnClickListener { toggleBottomSheetBehaviorState() }
        bottomSheetTitle.text = getString(CityString.data[coffeeShopsViewModel.twCity]!!)
        bottomSheetTitle.setOnClickListener { createPopupMenu(bottomSheetTitle) }
    }

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val upShiftRatio = 0.7f // BottomSheet shift 1 unit, map shift 0.7 unit
            val distance = slideOffset * bottomSheet.resources.getDimensionPixelSize(R.dimen.item_height)
            if (distance > 0) {
                container.translationY = -distance * resources.displayMetrics.density * upShiftRatio
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheetIndicator.setImageResource(R.drawable.down)
                    bottomSheetTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.sort_down, 0)
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomSheetIndicator.setImageResource(R.drawable.up)
                    bottomSheetTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.sort_up, 0)
                }
            }
        }
    }

    private fun toggleBottomSheetBehaviorState() {
        bottomSheet.let {
            val bottomSheetBehavior = BottomSheetBehavior.from(it)
            bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
                else -> bottomSheetBehavior.state
            }
        }
    }

    private fun setupBottomSheetData() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CoffeeShopsSimpleListAdapter(
            coffeeShopsViewModel.getDistancePairFromPosition(positioningViewModel.latLng.value!!),
            { id: String? -> id?.let { ShopDetailActivity.go(this, it) } },
            { id: String? ->
                id?.let {
                    val coffeeShop = coffeeShopsViewModel.current[it]!!
                    setCenter(LatLng(coffeeShop.latitude?.toDouble() ?: .0, coffeeShop.longitude?.toDouble() ?: .0))
                }
            }
        )
    }

    private fun initFloatingButton() {
        floatingButton.setOnClickListener {
            positioningViewModel.reloadPosition()
            updateViewData()
        }
    }

    private fun updateViewData() {
        val coffeeShopPair = coffeeShopsViewModel.getDistancePairFromPosition(positioningViewModel.latLng.value!!)
        recyclerView.adapter?.let {
            (it as CoffeeShopsSimpleListAdapter).updateData(coffeeShopPair)
        }
    }

    //================================================================================
    // Menu
    //================================================================================

    private fun createPopupMenu(view: View?): Boolean {
        val popupMenu = PopupMenu(this, view!!)
        popupMenu.menuInflater.inflate(R.menu.map_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.location_north).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_middle).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_south).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_east).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.location_isolated).subMenu.clearHeader()
        popupMenu.setOnMenuItemClickListener { item -> this.onMenuItemSelected(item!!) }
        popupMenu.show()
        return true
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
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