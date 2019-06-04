package scottychang.cafe_walker.adapter

import android.support.v7.util.DiffUtil
import scottychang.cafe_walker.model.CoffeeShop

class CoffeeShopsDiffCallback(
    private val oldList: List<Pair<CoffeeShop, Double>>?,
    private val newList: List<Pair<CoffeeShop, Double>>?
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList?.size ?: 0

    override fun getNewListSize(): Int = newList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)?.first
        val newItem = newList?.get(newItemPosition)?.first
        return oldItem?.equals(newItem) ?: false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)
        val newItem = newList?.get(newItemPosition)
        return oldItem?.equals(newItem) ?: false
    }
}