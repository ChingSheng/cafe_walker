package scottychang.cafe_nomad_mobile.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import scottychang.cafe_nomad_mobile.R
import scottychang.cafe_nomad_mobile.adapter.viewholder.CoffeeShopViewHolder
import scottychang.cafe_nomad_mobile.model.CoffeeShop
import java.lang.ref.WeakReference

class CoffeeShopsSimpleListAdapter(
    private var data: List<Pair<CoffeeShop, Double>>?,
    private val onItemClick: (id: String?) -> Unit = {},
    private val onItemLongClick: (id: String?) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var referenceRecyclerView: WeakReference<RecyclerView> = WeakReference<RecyclerView>(null)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        referenceRecyclerView = WeakReference(recyclerView)
    }

    fun updateData(newData: List<Pair<CoffeeShop, Double>>?) {
        val result = DiffUtil.calculateDiff(CoffeeShopsDiffCallback(data, newData))
        data = newData
        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_coffee_simple_item, parent, false)
        return CoffeeShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { onItemClick.invoke(data?.get(position)?.first?.id) }
        holder.itemView.setOnLongClickListener {onItemLongClick.invoke(data?.get(position)?.first?.id)
            true}
        (holder as CoffeeShopViewHolder).onBind(data?.get(position)!!)
    }
}