package com.pleon.buyt.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.BaseViewHolder
import kotlinx.android.synthetic.main.item_detail.view.*

class ItemDetailAdapter : RecyclerView.Adapter<ItemDetailAdapter.ItemDetailHolder>() {

    var items = mutableListOf<Item>()
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
            itemView.itemQuantity.text = item.quantity.toString()
            itemView.itemTotalPrice.text = item.totalPrice.toString()
        }
    }
}
