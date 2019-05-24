package com.pleon.buyt.ui.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.adapter.StoresAdapter.StoreHolder
import com.pleon.buyt.util.FormatterUtil.formatPrice
import kotlinx.android.synthetic.main.store_list_row.view.*
import javax.inject.Inject

class StoresAdapter @Inject constructor(private val cxt: Application) : Adapter<StoreHolder>() {

    var storeDetails = listOf<StoreDetail>()
        set(storeDetails) {
            field = storeDetails
            notifyDataSetChanged()
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.store_list_row, parent, false)
        return StoreHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoreHolder, position: Int) {
        holder.bindStore(storeDetails[position])
    }

    override fun getItemCount() = storeDetails.size

    override fun getItemId(position: Int) = storeDetails[position].store.storeId

    fun getStore(position: Int) = storeDetails[position].store

    inner class StoreHolder(view: View) : BaseViewHolder(view) {
        fun bindStore(storeDetail: StoreDetail) {
            itemView.storeIcon.setImageResource(storeDetail.store.category.storeImageRes)
            itemView.storeName.text = storeDetail.store.name
            itemView.purchaseCount.text = cxt.resources.getQuantityString(R.plurals.store_detail_purchase_count, storeDetail.purchaseCount, storeDetail.purchaseCount)
            itemView.totalSpending.text = cxt.resources.getQuantityString(R.plurals.price_with_suffix, storeDetail.totalSpending, formatPrice(storeDetail.totalSpending))
            itemView.circular_reveal.alpha = 0f // for the case of undo of deleted item
        }
    }
}
