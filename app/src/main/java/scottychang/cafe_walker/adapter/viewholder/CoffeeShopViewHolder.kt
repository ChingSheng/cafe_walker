package scottychang.cafe_walker.adapter.viewholder

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import scottychang.cafe_walker.R
import scottychang.cafe_walker.model.CoffeeShop

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

    private fun setPluginString(
        context: Context,
        coffeeShop: Pair<CoffeeShop, Double>
    ): String? {
        return (if (getStatusSymbol(context, coffeeShop.first.socket) != null) (context.getString(
            R.string.socket,
            getStatusSymbol(context, coffeeShop.first.socket) + "\t"
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

    private fun setPriceString(
        coffeeShop: Pair<CoffeeShop, Double>,
        context: Context
    ): String {
        return (if (coffeeShop.first.cheap!! > 0) (context.getString(
            R.string.cheap,
            coffeeShop.first.cheap
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