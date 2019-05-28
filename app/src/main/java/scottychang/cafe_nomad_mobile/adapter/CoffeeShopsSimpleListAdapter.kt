package scottychang.cafe_nomad_mobile.adapter

import android.support.design.widget.BottomSheetBehavior
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.adapter.viewholder.CoffeeShopTitleViewHolder
import scottychang.cafe_nomad_mobile.adapter.viewholder.CoffeeShopViewHolder
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
}