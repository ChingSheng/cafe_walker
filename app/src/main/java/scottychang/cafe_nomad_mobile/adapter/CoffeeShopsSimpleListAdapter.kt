package scottychang.cafe_nomad_mobile.adapter

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.model.CoffeeShop
import java.lang.ref.WeakReference


class CoffeeShopsSimpleListAdapter(
    private val title: String,
    private var data: List<Pair<CoffeeShop, Double>>?,
    private val onItemClick: (id: String?) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TITLE_TYPE = 0
    private val ITEM_TYPE = 1
    private var referenceRecyclerView: WeakReference<RecyclerView> = WeakReference<RecyclerView>(null)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        referenceRecyclerView = WeakReference(recyclerView)
        BottomSheetBehavior.from(referenceRecyclerView.get()).setBottomSheetCallback(bottomSheetBehaviorCallback)
    }

    fun updateData(newData: List<Pair<CoffeeShop, Double>>?) {
        val result = DiffUtil.calculateDiff(CoffeeShopsDiffCallback(data, newData))
        data = newData
        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = data?.size?.plus(1) ?: 1

    override fun getItemViewType(position: Int): Int = if (position == 0) TITLE_TYPE else ITEM_TYPE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TITLE_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_coffee_simple_title, parent, false)
            view.setOnClickListener { toggleBottomSheetBehaviorState() }
            CoffeeShopTitleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_coffee_simple_item, parent, false)
            CoffeeShopViewHolder(view)
        }

    private fun toggleBottomSheetBehaviorState() {
        referenceRecyclerView.get()?.let {
            val bottomSheetBehavior = BottomSheetBehavior.from(it)
            bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
                else -> bottomSheetBehavior.state
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = if (position == TITLE_TYPE) {
        (holder as CoffeeShopTitleViewHolder).onBind(title, when(BottomSheetBehavior.from(referenceRecyclerView.get()).state) {
            BottomSheetBehavior.STATE_EXPANDED -> R.drawable.down
            else -> R.drawable.up
        })
    } else {
        holder.itemView.setOnClickListener { onItemClick.invoke(data?.get(position -1)?.first?.id) }
        (holder as CoffeeShopViewHolder).onBind(data?.get(position - 1)!!)
    }

    class CoffeeShopViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun onBind(coffeeShop: Pair<CoffeeShop, Double>) {
            itemView.findViewById<TextView>(R.id.shop_name).text = coffeeShop.first.name
            itemView.findViewById<TextView>(R.id.distance).text = setDistance(coffeeShop.second)
            setMetaData(coffeeShop)
        }

        private fun setMetaData(coffeeShop: Pair<CoffeeShop, Double>) {
            val metadata = itemView.findViewById<TextView>(R.id.shop_metadata_simple)
            metadata.text = setMetaString(coffeeShop)
            metadata.visibility = if (metadata.text.length > 0) View.VISIBLE else View.GONE
        }

        private fun setMetaString(coffeeShop: Pair<CoffeeShop, Double>): String {
            val context = itemView.context
            return setPluginString(context, coffeeShop) +
                    setWifiString(coffeeShop, context) +
                    setPriceString(coffeeShop, context)
        }

        private fun setPriceString(
            coffeeShop: Pair<CoffeeShop, Double>,
            context: Context
        ): String {
            return (if (coffeeShop.first.cheap!! > 0) (context.getString(
                R.string.cheap,
                coffeeShop.first.cheap
            )) else "")
        }

        private fun setWifiString(
            coffeeShop: Pair<CoffeeShop, Double>,
            context: Context
        ): String {
            return (if (coffeeShop.first.wifi!! > 0) (context.getString(
                R.string.wifi,
                coffeeShop.first.wifi
            ) + "\t") else "")
        }

        private fun setPluginString(
            context: Context,
            coffeeShop: Pair<CoffeeShop, Double>
        ): String? {
            return (if (getStatusSymbol(context, coffeeShop.first.socket) != null) (context.getString(
                R.string.socket,
                getStatusSymbol(context, coffeeShop.first.socket) + "\t"
            )) else "")
        }

        private fun getStatusSymbol(context: Context, input: String?): String? =
            when (input) {
                "yes" -> context.getString(R.string.yes)
                "no" -> context.getString(R.string.no)
                "maybe" -> context.getString(R.string.maybe)
                else -> null
            }

        private fun setDistance(second: Double): String =
            if (second < 1000) {
                second.toInt().toString() + "m"
            } else {
                String.format("%.1f", second / 1000) + "km"
            }
    }

    class CoffeeShopTitleViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun onBind(title: String, @DrawableRes drawable: Int) {
            itemView.findViewById<TextView>(R.id.title).text = title
            itemView.findViewById<ImageView>(R.id.swipe_icon).setImageResource(drawable)
        }
    }

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Do nothing
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            (bottomSheet as RecyclerView).findViewHolderForAdapterPosition(0)?.let {
                val imageView = it.itemView.findViewById(R.id.swipe_icon) as ImageView
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> imageView.setImageResource(R.drawable.down)
                    BottomSheetBehavior.STATE_COLLAPSED -> imageView.setImageResource(R.drawable.up)
                }
            }
        }
    }
}