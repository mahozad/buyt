package com.pleon.buyt.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pleon.buyt.R
import com.pleon.buyt.model.PurchaseDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.adapter.PurchaseDetailAdapter.PurchaseHolder
import kotlinx.android.synthetic.main.purchase_detail.view.*

class PurchaseDetailAdapter : RecyclerView.Adapter<PurchaseHolder>() {

    var items = mutableListOf<PurchaseDetail>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.purchase_detail, parent, false)
        return PurchaseHolder(itemView)
    }

    override fun onBindViewHolder(holder: PurchaseHolder, position: Int) {
        holder.bindItem(items[position])
    }

    inner class PurchaseHolder(view: View) : BaseViewHolder(view) {
        fun bindItem(purchaseDetail: PurchaseDetail) {
            itemView.storeName.text = purchaseDetail.store[0].name
            itemView.itemDetails.adapter = ItemDetailAdapter().apply { items = purchaseDetail.item.toMutableList() }
            var totalCost = 0L
            for (item in purchaseDetail.item) totalCost += item.totalPrice
            itemView.totalCost.text = totalCost.toString()
        }
    }
}

