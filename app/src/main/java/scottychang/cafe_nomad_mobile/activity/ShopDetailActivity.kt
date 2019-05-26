package scottychang.cafe_nomad_mobile.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.model.CoffeeShop
import scottychang.cafe_nomad_mobile.viewmodel.CoffeeShopsViewModel
import java.net.URLDecoder




class ShopDetailActivity : AppCompatActivity() {
    private val googleMapPackage = "com.google.android.apps.maps"

    private val button : FloatingActionButton by bindView(R.id.location)
    private val name: TextView by bindView(R.id.name)
    private val address: TextView by bindView(R.id.address)
    private val mrt: TextView by bindView(R.id.mrt)
    private val site: TextView by bindView(R.id.site)
    private val socket: TextView by bindView(R.id.socket)
    private val limitTime: TextView by bindView(R.id.time_limit)
    private val standing: TextView by bindView(R.id.standing)
    private val ratingWifi: TextView by bindView(R.id.wifi)
    private val ratingSeat: TextView by bindView(R.id.seat)
    private val ratingTasty: TextView by bindView(R.id.tasty)
    private val ratingCheap: TextView by bindView(R.id.cheap)
    private val ratingQuiet: TextView by bindView(R.id.quiet)
    private val ratingMusic: TextView by bindView(R.id.music)
    private val openingTime: TextView by bindView(R.id.opening_time)

    fun <T : View> bindView(@IdRes resId: Int): Lazy<T> = lazy { findViewById<T>(resId) }

    companion object {
        val KEY_ID = "id"
        fun go(context: Context, id:String) {
            val intent = Intent(context, ShopDetailActivity::class.java)
            intent.putExtra(KEY_ID, id)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_detail)

        val coffeeShopsViewModel = ViewModelProviders.of(this).get(CoffeeShopsViewModel::class.java)

        intent.getStringExtra(KEY_ID)?.let {
            coffeeShopsViewModel.current.get(it)?.let { coffeeShop: CoffeeShop ->
                name.text = coffeeShop.name
                address.text = coffeeShop.address
                button.setOnClickListener(buttonClickListener(coffeeShop))
                initSocket(coffeeShop.socket)
                initLimitTime(coffeeShop.limited_time)
                initStanding(coffeeShop.standing_desk)
                initMRT(coffeeShop.mrt)
                initSite(coffeeShop.url)
                initRating(coffeeShop)
                initOpeningTime(coffeeShop.open_time)
            } ?: run {
                // Show something wrong
            }
        } ?: run {
            // Show something wrong
        }
    }

    private fun initSocket(data: String?) {
        if (valid(data) && validState(data!!)) {
            socket.text = getString(R.string.socket, when(data) {
                "yes" -> getString(R.string.socket_yes)
                "no" -> getString(R.string.socket_no)
                "maybe" -> getString(R.string.socket_maybe)
                else -> ""
            })
        } else {
            socket.visibility = View.GONE
        }
    }

    private fun initLimitTime(data: String?) {
        if (valid(data) && validState(data!!)) {
            limitTime.text = getString(R.string.limit_time, when(data) {
                "yes" -> getString(R.string.limit_time_yes)
                "no" -> getString(R.string.limit_time_no)
                "maybe" -> getString(R.string.limit_time_maybe)
                else -> ""
            })
        } else {
            limitTime.visibility = View.GONE
        }
    }

    private fun initStanding(data: String?) {
        if (valid(data) && data.equals("yes")) {
            socket.text = getString(R.string.standing_yes)
        } else {
            standing.visibility = View.GONE
        }
    }

    private fun validState(data: String): Boolean {
        return data.equals("yes") || data.equals("no") || data.equals("maybe")
    }

    private fun buttonClickListener(coffeeShop: CoffeeShop): View.OnClickListener {
        return View.OnClickListener {
            val s = "geo:%s,%s?q=%s".format(coffeeShop.latitude.toString(), coffeeShop.longitude.toString(), coffeeShop.name)
            val gmmIntentUri = Uri.parse(s)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage(googleMapPackage)
            startActivity(mapIntent)
        }
    }

    private fun initSite(site: String?) {
        if (valid(site)) {
            val afterDecode = URLDecoder.decode(site, "UTF-8")
            this.site.text = afterDecode
            this.site.paint.flags = Paint.UNDERLINE_TEXT_FLAG
            this.site.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(site))
                    startActivity(intent)
                }
            })
        } else {
            this.site.visibility = View.GONE
        }
    }

    private fun initMRT(data: String?) {
        if (valid(data)) {
            mrt.text = if (data!!.length > 8) getString(R.string.info_arrival, data) else getString(R.string.info_mrt, data)
        } else {
            mrt.visibility = View.GONE
        }
    }

    private fun initRating(coffeeShop: CoffeeShop) {
        ratingWifi.text = getString(R.string.rating_wifi, coffeeShop.wifi)
        ratingSeat.text = getString(R.string.rating_seat, coffeeShop.seat)
        ratingTasty.text = getString(R.string.rating_tasty, coffeeShop.tasty)
        ratingCheap.text = getString(R.string.rating_cheap, coffeeShop.cheap)
        ratingQuiet.text = getString(R.string.rating_quiet, coffeeShop.quiet)
        ratingMusic.text = getString(R.string.rating_music, coffeeShop.music)
    }

    private fun initOpeningTime(openTime: String?) {
        openingTime.text = if (valid(openTime)) openTime else getString(R.string.no_data)
    }

    private fun valid(input: String?): Boolean = input != null && input.length > 0



}