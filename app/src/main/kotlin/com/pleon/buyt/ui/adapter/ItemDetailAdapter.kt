package com.pleon.buyt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.adapter.ItemDetailAdapter.ItemDetailHolder
import com.pleon.buyt.util.NumberFormatUtil.formatPrice
import kotlinx.android.synthetic.main.item_detail.view.*

class ItemDetailAdapter(private val cxt: Context) : Adapter<ItemDetailHolder>() {

    var items = listOf<Item>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemDetailHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_detail, parent, false)
        return ItemDetailHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemDetailHolder, position: Int) {
        holder.bindItem(items[position])
    }

    inner class ItemDetailHolder(view: View) : BaseViewHolder(view) {
        fun bindItem(item: Item) {
            itemView.itemName.text = item.name
            itemView.itemQuantity.text = cxt.getString(R.string.item_quantity,
                    item.quantity.quantity, cxt.getString(item.quantity.unit.nameRes))
            itemView.itemTotalPrice.text = cxt.getString(R.string.purchase_detail_price, formatPrice(item.totalPrice))
        }
    }
}
