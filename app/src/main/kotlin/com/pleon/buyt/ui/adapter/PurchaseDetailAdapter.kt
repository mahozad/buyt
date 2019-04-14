package com.pleon.buyt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.model.PurchaseDetail
import com.pleon.buyt.ui.BaseViewHolder
import com.pleon.buyt.ui.DateHeaderDecoration.StickyHeaderInterface
import ir.huri.jcal.JalaliCalendar
import kotlinx.android.synthetic.main.date_header.view.*
import kotlinx.android.synthetic.main.purchase_detail.view.*
import org.jetbrains.anko.configuration
import java.util.*

class PurchaseDetailAdapter(private val cxt: Context) : Adapter<ViewHolder>(), StickyHeaderInterface {

    var items = mutableListOf<Any>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        // Just as an example, return 0 or 1 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return if (items[position] is PurchaseDetail) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> PurchaseHolder(inflater.inflate(com.pleon.buyt.R.layout.purchase_detail, parent, false))
            else -> DateHolder(inflater.inflate(com.pleon.buyt.R.layout.date_header, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> (holder as PurchaseHolder).bindItem(items[position] as PurchaseDetail)
            else -> (holder as DateHolder).bindItem(items[position] as Date)
        }
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemPosition = itemPosition
        var headerPosition = 0
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition
                break
            }
            itemPosition -= 1
        } while (itemPosition >= 0)
        return headerPosition
    }

    override fun bindHeaderData(header: View, headerPosition: Int) {
        header.findViewById<TextView>(R.id.dateText).text = formatDate(items[headerPosition] as Date)
    }

    override fun getHeaderLayout(headerPosition: Int) = com.pleon.buyt.R.layout.date_header

    override fun isHeader(itemPosition: Int) = items[itemPosition] is Date

    private fun formatDate(date: Date): String {
        val jalaliCalendar = JalaliCalendar(date)
        return String.format(cxt.configuration.locale, "%s %d %s %d",
                jalaliCalendar.dayOfWeekString, jalaliCalendar.day,
                jalaliCalendar.monthString, jalaliCalendar.year)
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

    inner class DateHolder(view: View) : BaseViewHolder(view) {
        fun bindItem(date: Date) {
            itemView.dateText.text = formatDate(date)
        }
    }
}

