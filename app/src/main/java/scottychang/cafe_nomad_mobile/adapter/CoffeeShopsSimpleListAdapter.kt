package scottychang.cafe_nomad_mobile.adapter

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.model.CoffeeShop


class CoffeeShopsSimpleListAdapter(
    private val title: String,
    private val data: List<Pair<CoffeeShop, Double>>?,
    private val onItemClick: (position: Int) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TITLE_TYPE = 0
    private val ITEM_TYPE = 1
    private var referenceRecyclerView: WeakReference<RecyclerView> = WeakReference<RecyclerView>(null)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        referenceRecyclerView = WeakReference(recyclerView)
        BottomSheetBehavior.from(referenceRecyclerView.get()).setBottomSheetCallback(bottomSheetBehaviorCallback)
    }

    override fun getItemCount(): Int {
        return data?.size?.plus(1) ?: 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TITLE_TYPE else ITEM_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TITLE_TYPE) {
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
        holder as CoffeeShopViewHolder
        holder.itemView.setOnClickListener { onItemClick.invoke(position - 1) }
        holder.onBind(data?.get(position - 1)!!)
    }

    class CoffeeShopViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun onBind(coffeeShop: Pair<CoffeeShop, Double>) {
            itemView.findViewById<TextView>(R.id.shop_name).text = coffeeShop.first.name
            itemView.findViewById<TextView>(R.id.shop_metadata_simple).text = getMetaString(coffeeShop)
            itemView.findViewById<TextView>(R.id.distance).text = getDistance(coffeeShop.second)
        }

        private fun getMetaString(coffeeShop: Pair<CoffeeShop, Double>): String {
            val context = itemView.context
            return String.format(
                context.getString(R.string.socket),
                getStatusSymbol(context,coffeeShop.first.socket)
            ) + ", " + (context.getString(R.string.wifi) + coffeeShop.first.wifi)
        }

        private fun getStatusSymbol(context: Context, input: String?): String =
            when (input) {
                "yes" -> context.getString(R.string.yes)
                "no" -> context.getString(R.string.no)
                "maybe" -> context.getString(R.string.maybe)
                else -> context.getString(R.string.unkonwn)
            }

        private fun getDistance(second: Double): String =
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
            bottomSheet as RecyclerView
            bottomSheet.findViewHolderForAdapterPosition(0)?.let {
                val imageView = it.itemView.findViewById(R.id.swipe_icon) as ImageView
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> imageView.setImageResource(R.drawable.down)
                    BottomSheetBehavior.STATE_COLLAPSED -> imageView.setImageResource(R.drawable.up)
                }
            }
        }
    }
}