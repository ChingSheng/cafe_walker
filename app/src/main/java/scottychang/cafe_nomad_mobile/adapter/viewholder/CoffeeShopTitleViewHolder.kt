package scottychang.cafe_nomad_mobile.adapter.viewholder

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import scottychang.cafe_nomad_mobile.R

class CoffeeShopTitleViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun onBind(title: String, @DrawableRes drawable: Int) {
        itemView.findViewById<TextView>(R.id.title).text = title
        itemView.findViewById<ImageView>(R.id.swipe_icon).setImageResource(drawable)
    }
}